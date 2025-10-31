import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AnalysisState } from './analysis.models';

export const selectAnalysisState = createFeatureSelector<AnalysisState>('analysis');

export const selectHistory = createSelector(
  selectAnalysisState,
  state => state.history
);
export const selectHistoryLoading = createSelector(
  selectAnalysisState,
  state => state.loading.history
);
export const selectHistoryError = createSelector(
  selectAnalysisState,
  state => state.error.history
);

export const selectDetail = createSelector(
  selectAnalysisState,
  state => state.detail
);
export const selectDetailLoading = createSelector(
  selectAnalysisState,
  state => state.loading.detail
);
export const selectDetailError = createSelector(
  selectAnalysisState,
  state => state.error.detail
);

export const selectUploadLoading = createSelector(
  selectAnalysisState,
  state => state.loading.upload
);
export const selectUploadError = createSelector(
  selectAnalysisState,
  state => state.error.upload
);

export const selectProgress = (id: number) =>
  createSelector(selectAnalysisState, state => state.progress[id] ?? 0);
