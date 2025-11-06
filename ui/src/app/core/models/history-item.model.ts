import { AnalysisStatus } from './analysis-status.enum';

export interface HistoryItem {
  id: number;
  fileName: string;
  fileSizeBytes: number;
  processDurationMillis?: number;
  avg?: number;
  stdDev?: number;
  status: AnalysisStatus;
}
