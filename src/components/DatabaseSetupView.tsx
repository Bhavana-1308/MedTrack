import React, { useState } from 'react';
import { Check, Copy, Database, Terminal } from 'lucide-react';
import { JAVA_PROJECT_FILES } from '../data/javaProjectLoader';

const TABLES = [
  {
    name: 'users',
    pk: 'user_id',
    columns: 'full_name, username (UNIQUE), email (UNIQUE), password_hash, phone, created_at',
    relationship: '1 User → Many Medicines, Reminders, and History rows',
  },
  {
    name: 'medicines',
    pk: 'medicine_id',
    columns: 'user_id (FK), medicine_name, medicine_type (PRESCRIPTION | OTC), dosage, frequency, start_date, end_date, doctor_name, purpose_category, notes',
    relationship: 'FK user_id → users(user_id) ON DELETE CASCADE',
  },
  {
    name: 'reminders',
    pk: 'reminder_id',
    columns: 'medicine_id (FK), user_id (FK), reminder_time, reminder_label, is_active, created_at',
    relationship: 'FK medicine_id → medicines(medicine_id) CASCADE; FK user_id → users(user_id) CASCADE',
  },
  {
    name: 'medicine_history',
    pk: 'history_id',
    columns: 'medicine_id (FK), reminder_id (FK NULL), user_id (FK), dose_date, scheduled_time, taken_at, status (TAKEN | MISSED | PENDING), remarks',
    relationship: 'FK medicine_id → medicines CASCADE; FK reminder_id → reminders SET NULL',
  },
];

const SAMPLE_IO = [
  {
    action: 'User Registration',
    input: 'Full Name: Priya Verma, Username: priya_26, Email: priya@college.edu, Password: CarePass99',
    output: 'User row inserted with SHA-256 password_hash; dialog confirms "Account created successfully!"',
  },
  {
    action: 'Add Prescription Medicine',
    input: 'Name: Amoxicillin, Type: PRESCRIPTION, Dosage: 500 mg, Frequency: Twice Daily, Doctor: Dr. Meera Nair, Dates: Today to +7 days',
    output: 'PrescriptionMedicine object persisted in medicines table; Dashboard Total Medicines increments by 1.',
  },
  {
    action: 'Validation Error Handling',
    input: 'Start Date: 2026-11-20, End Date: 2026-11-15 (earlier than start)',
    output: 'InvalidInputException thrown by InputValidator; JOptionPane warns "End Date cannot be earlier than Start Date."',
  },
  {
    action: 'Background Reminder Alert',
    input: 'Reminder set at 08:30 matching system clock while Dashboard is open',
    output: 'ReminderSchedulerThread wakes up, invokes SwingUtilities.invokeLater, beeps, and displays "Time to take your medicine!" popup.',
  },
  {
    action: 'CSV Report Export',
    input: 'Click "Export History CSV" on Dashboard',
    output: 'FileReportService writes exports/medicine_history_arjun_med.csv using Exportable.toCsvRow() and logs action to logs/reminder_activity.log.',
  },
];

export const DatabaseSetupView: React.FC = () => {
  const [copiedSql, setCopiedSql] = useState(false);
  const schemaFile = JAVA_PROJECT_FILES.find((f) => f.path === 'database/schema.sql');

  function handleCopySql() {
    if (!schemaFile) return;
    navigator.clipboard.writeText(schemaFile.content);
    setCopiedSql(true);
    window.setTimeout(() => setCopiedSql(false), 1800);
  }

  return (
    <div className="space-y-8">
      {/* 4 MySQL Tables Overview */}
      <section className="bg-white border border-slate-200 rounded-xl p-6 space-y-4">
        <div className="flex items-center gap-2">
          <Database className="w-4 h-4 text-teal-700" />
          <h2 className="text-base font-semibold text-slate-900">
            MySQL Relational Schema (medicine_reminder_db)
          </h2>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-200 text-xs font-semibold text-slate-500">
                <th className="py-2.5 pr-4">Table</th>
                <th className="py-2.5 pr-4">Primary Key</th>
                <th className="py-2.5 pr-4">Columns</th>
                <th className="py-2.5">Foreign Key Relationships</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs">
              {TABLES.map((t) => (
                <tr key={t.name}>
                  <td className="py-3 pr-4 font-mono font-semibold text-teal-800">{t.name}</td>
                  <td className="py-3 pr-4 font-mono text-slate-700">{t.pk}</td>
                  <td className="py-3 pr-4 text-slate-600 leading-relaxed">{t.columns}</td>
                  <td className="py-3 text-slate-700 leading-relaxed">{t.relationship}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Step-by-Step Execution Instructions */}
      <section className="bg-white border border-slate-200 rounded-xl p-6 space-y-5">
        <div className="flex items-center gap-2">
          <Terminal className="w-4 h-4 text-teal-700" />
          <h2 className="text-base font-semibold text-slate-900">
            Step-by-Step Execution Guide (Local Machine)
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm text-slate-700">
          <div className="space-y-3">
            <h3 className="text-xs font-semibold text-slate-900">01. Download &amp; Extract Project</h3>
            <p className="text-xs leading-relaxed text-slate-600">
              Click <strong>Download Java Project .zip</strong> in the top bar and extract the{' '}
              <code className="font-mono text-slate-800">MedicineReminderSystem/</code> folder.
            </p>

            <h3 className="text-xs font-semibold text-slate-900 pt-2">02. Initialize MySQL Database</h3>
            <pre className="bg-slate-950 text-slate-100 rounded-lg p-3 text-xs font-mono overflow-x-auto">
              mysql -u root -p &lt; database/schema.sql
            </pre>
            <p className="text-xs leading-relaxed text-slate-600">
              Then update <code className="font-mono text-slate-800">config/db.properties</code> with your MySQL username and password.
            </p>
          </div>

          <div className="space-y-3">
            <h3 className="text-xs font-semibold text-slate-900">03. Add MySQL Connector/J JAR</h3>
            <p className="text-xs leading-relaxed text-slate-600">
              Download <code className="font-mono text-slate-800">mysql-connector-j-8.x.x.jar</code> from Oracle MySQL Downloads and place it inside <code className="font-mono text-slate-800">lib/</code>. In VS Code, add it under <em>Java Projects → Referenced Libraries</em>. In IntelliJ IDEA, add it via <em>File → Project Structure → Libraries</em>.
            </p>

            <h3 className="text-xs font-semibold text-slate-900 pt-2">04. Compile &amp; Run via Terminal</h3>
            <pre className="bg-slate-950 text-slate-100 rounded-lg p-3 text-xs font-mono overflow-x-auto">
{`mkdir out
javac -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" com.medreminder.Main`}
            </pre>
          </div>
        </div>
      </section>

      {/* Sample Inputs & Outputs */}
      <section className="bg-white border border-slate-200 rounded-xl p-6 space-y-4">
        <h2 className="text-base font-semibold text-slate-900">Sample Inputs &amp; Expected Outputs</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-200 text-xs font-semibold text-slate-500">
                <th className="py-2.5 pr-4">Scenario</th>
                <th className="py-2.5 pr-4">Sample Input</th>
                <th className="py-2.5">Expected Output</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs">
              {SAMPLE_IO.map((row) => (
                <tr key={row.action}>
                  <td className="py-3 pr-4 font-semibold text-slate-900 whitespace-nowrap">{row.action}</td>
                  <td className="py-3 pr-4 text-slate-600 leading-relaxed">{row.input}</td>
                  <td className="py-3 text-slate-700 leading-relaxed">{row.output}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Full SQL Script */}
      {schemaFile && (
        <section className="bg-white border border-slate-200 rounded-xl overflow-hidden">
          <div className="px-5 py-3.5 border-b border-slate-200 flex items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold font-mono text-slate-900">database/schema.sql</h2>
              <p className="text-xs text-slate-500">Complete MySQL DDL + demo seed data</p>
            </div>
            <button
              type="button"
              onClick={handleCopySql}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg whitespace-nowrap shrink-0"
            >
              {copiedSql ? <Check className="w-3.5 h-3.5 text-emerald-700" /> : <Copy className="w-3.5 h-3.5" />}
              {copiedSql ? 'Copied SQL' : 'Copy SQL'}
            </button>
          </div>
          <pre className="bg-slate-950 text-slate-100 p-4 text-xs font-mono overflow-x-auto max-h-96 leading-relaxed">
            {schemaFile.content}
          </pre>
        </section>
      )}
    </div>
  );
};
