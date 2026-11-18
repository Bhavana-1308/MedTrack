export interface ProjectFile {
  path: string;
  name: string;
  category: string;
  content: string;
  lines: number;
}

const rawModules = import.meta.glob('../../java-project/**/*', {
  query: '?raw',
  import: 'default',
  eager: true,
}) as Record<string, string>;

function categorizePath(relPath: string): string {
  if (relPath.startsWith('database/')) return 'Database';
  if (relPath.startsWith('config/') || relPath.startsWith('lib/') || relPath === 'README.md') {
    return 'Configuration & Docs';
  }
  if (relPath.includes('/exception/')) return 'Exceptions';
  if (relPath.includes('/interfaces/')) return 'Interfaces';
  if (relPath.includes('/model/')) return 'Models (OOP)';
  if (relPath.includes('/dao/')) return 'JDBC DAOs';
  if (relPath.includes('/service/')) return 'Services & Threading';
  if (relPath.includes('/ui/')) return 'Swing UI';
  return 'Entry Point';
}

export const JAVA_PROJECT_FILES: ProjectFile[] = Object.entries(rawModules)
  .map(([fullPath, content]) => {
    const cleanPath = fullPath.replace(/^(\.\.\/)+java-project\//, '');
    const parts = cleanPath.split('/');
    return {
      path: cleanPath,
      name: parts[parts.length - 1],
      category: categorizePath(cleanPath),
      content,
      lines: content.split('\n').length,
    };
  })
  .sort((a, b) => a.path.localeCompare(b.path));

export interface ConceptMapping {
  concept: string;
  filePath: string;
  symbol: string;
  summary: string;
}

export const JAVA_CONCEPT_MAPPINGS: ConceptMapping[] = [
  {
    concept: 'Classes and Objects',
    filePath: 'src/com/medreminder/model/User.java',
    symbol: 'User, Medicine, Reminder, MedicineHistory, DAOs, Frames',
    summary: 'Every real-world entity (users, medicines, reminders, dose logs) and layer (DAOs, services, Swing frames) is modeled as a dedicated class instantiated at runtime.',
  },
  {
    concept: 'Encapsulation',
    filePath: 'src/com/medreminder/model/Medicine.java',
    symbol: 'private fields + public getters/setters & BaseEntity.setId()',
    summary: 'All model attributes are declared private and exposed through controlled getters and setters (with validation such as rejecting negative entity IDs).',
  },
  {
    concept: 'Inheritance',
    filePath: 'src/com/medreminder/model/PrescriptionMedicine.java',
    symbol: 'PrescriptionMedicine & OTCMedicine extends Medicine extends BaseEntity',
    summary: 'All domain models inherit common id and createdAt fields from BaseEntity; PrescriptionMedicine and OTCMedicine extend Medicine to add specialized fields.',
  },
  {
    concept: 'Polymorphism',
    filePath: 'src/com/medreminder/dao/MedicineDAO.java',
    symbol: 'Overridden getDosageInstructions() + overloaded searchMedicines()',
    summary: 'Runtime polymorphism overrides getDosageInstructions() and getSubTypeDetail() across PrescriptionMedicine and OTCMedicine; compile-time polymorphism overloads searchMedicines(userId, keyword) and searchMedicines(userId, keyword, type).',
  },
  {
    concept: 'Abstraction',
    filePath: 'src/com/medreminder/model/BaseEntity.java',
    symbol: 'abstract class BaseEntity & abstract class Medicine',
    summary: 'BaseEntity and Medicine define abstract contracts (getEntityType(), getSummary(), getDosageInstructions()) while DAOs abstract raw JDBC SQL queries away from the Swing UI.',
  },
  {
    concept: 'Interfaces',
    filePath: 'src/com/medreminder/interfaces/ReminderListener.java',
    symbol: 'ReminderListener & Exportable',
    summary: 'DashboardFrame implements ReminderListener to receive background thread callbacks; Medicine and MedicineHistory implement Exportable for unified CSV export.',
  },
  {
    concept: 'Collections (ArrayList, HashMap)',
    filePath: 'src/com/medreminder/dao/HistoryDAO.java',
    symbol: 'ArrayList<Medicine>, HashMap<Integer, Medicine>, HashMap<String, Integer>',
    summary: 'ArrayList stores ordered query results for Swing tables; HashMap<Integer, Medicine> provides O(1) medicine lookup; HashMap<String, Integer> aggregates dashboard KPI counts.',
  },
  {
    concept: 'Exception Handling',
    filePath: 'src/com/medreminder/exception/InvalidInputException.java',
    symbol: 'InvalidInputException, DatabaseOperationException, try-with-resources',
    summary: 'Custom checked exceptions separate validation errors from JDBC failures and display clean JOptionPane warnings instead of crashing the Swing application.',
  },
  {
    concept: 'Multithreading',
    filePath: 'src/com/medreminder/service/ReminderSchedulerThread.java',
    symbol: 'ReminderSchedulerThread extends Thread + SwingUtilities.invokeLater',
    summary: 'A background daemon thread polls active reminders every 15 seconds, prevents duplicate same-minute alerts via a HashMap, and dispatches UI dialogs safely on the Swing EDT.',
  },
  {
    concept: 'JDBC Connectivity',
    filePath: 'src/com/medreminder/dao/DBConnection.java',
    symbol: 'DriverManager.getConnection(), PreparedStatement, ResultSet',
    summary: 'DBConnection loads MySQL credentials from config/db.properties and all DAOs use parameterized PreparedStatement queries with RETURN_GENERATED_KEYS.',
  },
  {
    concept: 'File Handling',
    filePath: 'src/com/medreminder/service/FileReportService.java',
    symbol: 'BufferedWriter, BufferedReader, FileInputStream (Properties)',
    summary: 'FileReportService writes/reads logs/reminder_activity.log and exports CSV reports to exports/; DBConnection reads config/db.properties via FileInputStream.',
  },
];
