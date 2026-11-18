import { useState } from 'react';
import { Download } from 'lucide-react';
import { JAVA_PROJECT_FILES } from './data/javaProjectLoader';
import { createZipBlob } from './utils/zipBuilder';
import { SwingSimulator } from './components/SwingSimulator';
import { SourceExplorer } from './components/SourceExplorer';
import { DatabaseSetupView } from './components/DatabaseSetupView';
import { ConceptsMapView } from './components/ConceptsMapView';

type ActiveSection = 'demo' | 'source' | 'database' | 'concepts';

export default function App() {
  const [section, setSection] = useState<ActiveSection>('demo');
  const [selectedFilePath, setSelectedFilePath] = useState<string>('src/com/medreminder/Main.java');

  function handleDownloadZip() {
    const entries = JAVA_PROJECT_FILES.map((file) => ({
      path: `MedicineReminderSystem/${file.path}`,
      content: file.content,
    }));
    const blob = createZipBlob(entries);
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'MedicineReminderSystem-Java-MySQL.zip';
    link.click();
    URL.revokeObjectURL(url);
  }

  function handleOpenFileFromConcepts(path: string) {
    setSelectedFilePath(path);
    setSection('source');
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col">
      {/* Top Bar Contract: 3 zones separated by gap-8 */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-30">
        <div className="max-w-[1200px] mx-auto px-6 h-16 flex items-center justify-between gap-8">
          <a
            href="#top"
            onClick={(e) => {
              e.preventDefault();
              setSection('demo');
            }}
            className="text-base font-bold tracking-tight text-slate-900 whitespace-nowrap shrink-0"
          >
            MedTrack Java PBL
          </a>

          <nav className="flex items-center gap-6 overflow-x-auto">
            {(
              [
                ['demo', 'Live Demo'],
                ['source', 'Java Source'],
                ['database', 'MySQL & Setup'],
                ['concepts', 'OOP Concepts'],
              ] as const
            ).map(([key, label]) => (
              <button
                key={key}
                type="button"
                onClick={() => setSection(key)}
                className={`text-sm font-medium py-1 border-b-2 transition-colors whitespace-nowrap shrink-0 ${
                  section === key
                    ? 'border-teal-700 text-slate-900'
                    : 'border-transparent text-slate-500 hover:text-slate-900'
                }`}
              >
                {label}
              </button>
            ))}
          </nav>

          <div className="shrink-0">
            <button
              type="button"
              onClick={handleDownloadZip}
              className="inline-flex items-center gap-2 px-4 py-2 text-xs font-medium text-white bg-teal-700 hover:bg-teal-800 rounded-lg transition-colors whitespace-nowrap shrink-0"
            >
              <Download className="w-3.5 h-3.5" />
              Download Java .zip
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-[1200px] w-full mx-auto px-6 py-8 space-y-6">
        <div className="flex flex-wrap items-end justify-between gap-4 border-b border-slate-200 pb-5">
          <div>
            <p className="text-xs text-slate-500">
              Java Swing · MySQL 8 · JDBC · Multithreading · File Handling
            </p>
            <h1 className="text-2xl font-bold text-slate-900 mt-1">
              Medicine Reminder and Management System
            </h1>
          </div>
          <p className="text-xs font-mono tabular-nums text-slate-500">
            {JAVA_PROJECT_FILES.length} source &amp; config files ready
          </p>
        </div>

        {section === 'demo' && <SwingSimulator />}
        {section === 'source' && (
          <SourceExplorer selectedPath={selectedFilePath} onSelectPath={setSelectedFilePath} />
        )}
        {section === 'database' && <DatabaseSetupView />}
        {section === 'concepts' && <ConceptsMapView onOpenFile={handleOpenFileFromConcepts} />}
      </main>

      <footer className="border-t border-slate-200 bg-white">
        <div className="max-w-[1200px] mx-auto px-6 py-4 flex flex-wrap items-center justify-between gap-4 text-xs text-slate-500">
          <span>Medicine Reminder &amp; Management System — Java PBL Workbench</span>
          <span>Use Java Source &amp; Download .zip for local VS Code / IntelliJ execution</span>
        </div>
      </footer>
    </div>
  );
}
