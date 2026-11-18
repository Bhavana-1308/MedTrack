import React from 'react';
import { ArrowUpRight } from 'lucide-react';
import { JAVA_CONCEPT_MAPPINGS } from '../data/javaProjectLoader';

interface ConceptsMapViewProps {
  onOpenFile: (path: string) => void;
}

export const ConceptsMapView: React.FC<ConceptsMapViewProps> = ({ onOpenFile }) => {
  return (
    <div className="bg-white border border-slate-200 rounded-xl p-6 space-y-5">
      <div>
        <h2 className="text-base font-semibold text-slate-900">
          Java Concepts Implementation Map
        </h2>
        <p className="text-xs text-slate-500 mt-1">
          Every required OOP and core Java concept mapped to its exact source file and class implementation for PBL viva review.
        </p>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-200 text-xs font-semibold text-slate-500">
              <th className="py-2.5 pr-4">Java Concept</th>
              <th className="py-2.5 pr-4">Implemented Symbols</th>
              <th className="py-2.5 pr-4">How It Works</th>
              <th className="py-2.5 text-right">Source File</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-xs">
            {JAVA_CONCEPT_MAPPINGS.map((item, idx) => (
              <tr key={item.concept} className="hover:bg-slate-50 align-top">
                <td className="py-3.5 pr-4 font-semibold text-slate-900 whitespace-nowrap">
                  <span className="font-mono text-slate-400 mr-2 tabular-nums">
                    {String(idx + 1).padStart(2, '0')}.
                  </span>
                  {item.concept}
                </td>
                <td className="py-3.5 pr-4 font-mono text-teal-800">{item.symbol}</td>
                <td className="py-3.5 pr-4 text-slate-600 leading-relaxed max-w-md">{item.summary}</td>
                <td className="py-3.5 text-right whitespace-nowrap">
                  <button
                    type="button"
                    onClick={() => onOpenFile(item.filePath)}
                    className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-teal-800 bg-teal-50 hover:bg-teal-100 rounded-md whitespace-nowrap shrink-0 transition-colors"
                  >
                    <span className="font-mono">{item.filePath.split('/').pop()}</span>
                    <ArrowUpRight className="w-3 h-3" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
