import { AnalysisStatus } from './analysis-status.enum';

export interface AnalysisDetail {
  id: number;
  fileName: string;
  fileSizeBytes: number;
  tempFilePath: string;
  uploadedAt: string;
  processedAt: string | null;
  processDurationMillis: number;
  count: number;
  minValue: number;
  maxValue: number;
  avg: number;
  stdDev: number;
  skipCount: number;
  uniqueCount: number;
  status: AnalysisStatus;
}
