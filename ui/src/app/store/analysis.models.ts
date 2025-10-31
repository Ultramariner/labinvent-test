import { HistoryItem } from '../core/models/history-item.model';
import { AnalysisDetail } from '../core/models/analysis-detail.model';

export interface AnalysisState {
  history: HistoryItem[];
  detail: AnalysisDetail | null;
  progress: { [id: number]: number };
  loading: {
    upload: boolean;
    history: boolean;
    detail: boolean;
    delete: boolean;
    progress: boolean;
  };
  error: {
    upload?: string;
    history?: string;
    detail?: string;
    delete?: string;
    progress?: string;
  };
}

export interface AppState {
  analysis: AnalysisState;
}
