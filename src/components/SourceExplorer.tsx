import React, { useMemo, useState } from 'react';
import { Check, Copy, FileCode2, Folder } from 'lucide-react';
import { JAVA_PROJECT_FILES, ProjectFile } from '../data/javaProjectLoader';

interface SourceExplorerProps {
  selectedPath: string;
  onSelectPath: (path: string) => void;
}

const CATEGORY_ORDER = [
  'Database',
  'Configuration & Docs',
  'Entry Point',
  'Exceptions',
  'Interfaces',
  'Models (OOP)',
  'JDBC DAOs',
  'Services & Threading',
  'Swing UI',
];

export const SourceExplorer: React.FC<SourceExplorerProps> = ({ selectedPath, onSelectPath }) => {
  const [copied, setCopied] = useState(false);

  const grouped = useMemo(() => {
    const map = new Map<string, ProjectFile[]>();
    for (const cat of CATEGORY_ORDER) {
      map.set(cat, []);
    }
    for (const file of JAVA_PROJECT_FILES) {
      const list = map.get(file.category) ?? [];
      list.push(file);
      map.set(file.category, list);
    }
    return Array.from(map.entries()).filter(([, files]) => files.length > 0);
  }, []);

  const activeFile = useMemo(
    () => JAVA_PROJECT_FILES.find((f) => f.path === selectedPath) ?? JAVA_PROJECT_FILES[0],
    [selectedPath]
  );

  function handleCopy() {
    if (!activeFile) return;
    navigator.clipboard.writeText(activeFile.content);
    setCopied(true);
    window.setTimeout(() => setCopied(false), 1800);
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[290px_1fr] gap-5 items-start">
      {/* File Tree Sidebar */}
      <aside className="bg-white border border-slate-200 rounded-xl p-4 space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center gap-2 text-xs font-semibold text-slate-900">
            <Folder className="w-4 h-4 text-teal-700" />
            <span>MedicineReminderSystem/</span>
          </div>
          <span className="text-xs font-mono tabular-nums text-slate-500">
            {JAVA_PROJECT_FILES.length} files
          </span>
        </div>

        <div className="space-y-4 max-h-[680px] overflow-y-auto pr-1">
          {grouped.map(([category, files]) => (
            <div key={category} className="space-y-1">
              <p className="text-xs font-semibold text-slate-500 px-2">{category}</p>
              <div className="space-y-0.5">
                {files.map((file) => {
                  const isActive = activeFile?.path === file.path;
                  return (
                    <button
                      key={file.path}
                      type="button"
                      onClick={() => onSelectPath(file.path)}
                      className={`w-full text-left px-2.5 py-1.5 rounded-lg text-xs flex items-center justify-between gap-2 transition-colors ${
                        isActive
                          ? 'bg-teal-50 text-teal-900 font-semibold'
                          : 'text-slate-700 hover:bg-slate-50'
                      }`}
                    >
                      <span className="flex items-center gap-2 min-w-0">
                        <FileCode2 className={`w-3.5 h-3.5 shrink-0 ${isActive ? 'text-teal-700' : 'text-slate-400'}`} />
                        <span className="truncate font-mono">{file.name}</span>
                      </span>
                      <span className="text-[11px] font-mono tabular-nums text-slate-400 shrink-0">
                        {file.lines}L
                      </span>
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </aside>

      {/* Code Viewport */}
      {activeFile && (
        <div className="bg-white border border-slate-200 rounded-xl overflow-hidden min-w-0">
          <div className="px-5 py-3.5 border-b border-slate-200 flex flex-wrap items-center justify-between gap-3">
            <div className="min-w-0">
              <p className="text-sm font-semibold font-mono text-slate-900 truncate">
                {activeFile.path}
              </p>
              <p className="text-xs text-slate-500 mt-0.5">
                {activeFile.category} · {activeFile.lines} lines
              </p>
            </div>
            <button
              type="button"
              onClick={handleCopy}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg whitespace-nowrap shrink-0 transition-colors"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-700" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Copied' : 'Copy Source'}
            </button>
          </div>

          <div className="bg-slate-950 text-slate-100 overflow-x-auto max-h-[680px]">
            <table className="w-full text-xs font-mono leading-relaxed">
              <tbody>
                {activeFile.content.split('\n').map((line, idx) => (
                  <tr key={idx} className="hover:bg-slate-900/70">
                    <td className="select-none text-right text-slate-500 px-3 py-0.5 w-12 align-top tabular-nums border-r border-slate-800">
                      {idx + 1}
                    </td>
                    <td className="px-4 py-0.5 whitespace-pre text-slate-100">{line || ' '}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
