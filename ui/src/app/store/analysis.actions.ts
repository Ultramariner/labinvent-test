import { createAction, props } from '@ngrx/store';
import { HistoryItem } from '../core/models/history-item.model';
import { AnalysisDetail } from '../core/models/analysis-detail.model';

export const uploadFile = createAction(
  '[Analysis] Upload File',
  props<{ file: File }>()
);
export const uploadFileSuccess = createAction('[Analysis] Upload File Success');
export const uploadFileFailure = createAction(
  '[Analysis] Upload File Failure',
  props<{ error: string }>()
);

export const loadHistory = createAction('[Analysis] Load History');
export const loadHistorySuccess = createAction(
  '[Analysis] Load History Success',
  props<{ history: HistoryItem[] }>()
);
export const loadHistoryFailure = createAction(
  '[Analysis] Load History Failure',
  props<{ error: string }>()
);

export const loadDetail = createAction(
  '[Analysis] Load Detail',
  props<{ id: number }>()
);
export const loadDetailSuccess = createAction(
  '[Analysis] Load Detail Success',
  props<{ detail: AnalysisDetail }>()
);
export const loadDetailFailure = createAction(
  '[Analysis] Load Detail Failure',
  props<{ error: string }>()
);

export const deleteAnalysis = createAction(
  '[Analysis] Delete',
  props<{ id: number }>()
);
export const deleteAnalysisSuccess = createAction(
  '[Analysis] Delete Success',
  props<{ id: number }>()
);
export const deleteAnalysisFailure = createAction(
  '[Analysis] Delete Failure',
  props<{ error: string }>()
);

export const loadProgress = createAction(
  '[Analysis] Load Progress',
  props<{ id: number }>()
);
export const loadProgressSuccess = createAction(
  '[Analysis] Load Progress Success',
  props<{ id: number; progress: number }>()
);
export const loadProgressFailure = createAction(
  '[Analysis] Load Progress Failure',
  props<{ error: string }>()
);
export const analysisUpdated = createAction(
  '[Analysis] Updated',
  props<{
    id: number;
    status: string;
    progress?: number;
    fileName?: string;
    fileSizeBytes?: number;
    processDurationMillis?: number;
    avg?: number;
    stdDev?: number;
  }>()
);

export const cancelAnalysis = createAction(
  '[Analysis] Cancel',
  props<{ id: number }>()
);
export const restartAnalysis = createAction(
  '[Analysis] Restart',
  props<{ id: number }>()
);
