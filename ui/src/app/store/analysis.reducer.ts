import { createReducer, on } from '@ngrx/store';
import * as AnalysisActions from './analysis.actions';
import { AnalysisState } from './analysis.models';
import {AnalysisStatus} from '../core/models/analysis-status.enum';

export const initialState: AnalysisState = {
  history: [],
  detail: null,
  progress: {},
  loading: {
    upload: false,
    history: false,
    detail: false,
    delete: false,
    progress: false,
  },
  error: {
    upload: undefined,
    history: undefined,
    detail: undefined,
    delete: undefined,
    progress: undefined,
  }
};

export const analysisReducer = createReducer(
  initialState,

  on(AnalysisActions.uploadFile, state => ({
    ...state,
    loading: { ...state.loading, upload: true },
    error: { ...state.error, upload: undefined }
  })),
  on(AnalysisActions.uploadFileSuccess, state => ({
    ...state,
    loading: { ...state.loading, upload: false }
  })),
  on(AnalysisActions.uploadFileFailure, (state, { error }) => ({
    ...state,
    loading: { ...state.loading, upload: false },
    error: { ...state.error, upload: error }
  })),

  on(AnalysisActions.loadHistory, state => ({
    ...state,
    loading: { ...state.loading, history: true },
    error: { ...state.error, history: undefined }
  })),
  on(AnalysisActions.loadHistorySuccess, (state, { history }) => ({
    ...state,
    history,
    loading: { ...state.loading, history: false }
  })),
  on(AnalysisActions.loadHistoryFailure, (state, { error }) => ({
    ...state,
    loading: { ...state.loading, history: false },
    error: { ...state.error, history: error }
  })),

  on(AnalysisActions.loadDetail, state => ({
    ...state,
    loading: { ...state.loading, detail: true },
    error: { ...state.error, detail: undefined }
  })),
  on(AnalysisActions.loadDetailSuccess, (state, { detail }) => ({
    ...state,
    detail,
    loading: { ...state.loading, detail: false }
  })),
  on(AnalysisActions.loadDetailFailure, (state, { error }) => ({
    ...state,
    loading: { ...state.loading, detail: false },
    error: { ...state.error, detail: error }
  })),

  on(AnalysisActions.deleteAnalysis, state => ({
    ...state,
    loading: { ...state.loading, delete: true },
    error: { ...state.error, delete: undefined }
  })),
  on(AnalysisActions.deleteAnalysisSuccess, (state, { id }) => ({
    ...state,
    history: state.history.filter(item => item.id !== id),
    loading: { ...state.loading, delete: false }
  })),
  on(AnalysisActions.deleteAnalysisFailure, (state, { error }) => ({
    ...state,
    loading: { ...state.loading, delete: false },
    error: { ...state.error, delete: error }
  })),

  on(AnalysisActions.loadProgress, state => ({
    ...state,
    loading: { ...state.loading, progress: true },
    error: { ...state.error, progress: undefined }
  })),
  on(AnalysisActions.loadProgressSuccess, (state, { id, progress }) => ({
    ...state,
    progress: { ...state.progress, [id]: progress },
    loading: { ...state.loading, progress: false }
  })),
  on(AnalysisActions.loadProgressFailure, (state, { error }) => ({
    ...state,
    loading: { ...state.loading, progress: false },
    error: { ...state.error, progress: error }
  })),
  on(AnalysisActions.analysisUpdated, (state, payload) => ({
    ...state,
    history: state.history.map(item =>
      item.id === payload.id
        ? { ...item, ...payload, status: payload.status as AnalysisStatus }
        : item
    ),
    progress: payload.progress !== undefined
      ? { ...state.progress, [payload.id]: payload.progress }
      : state.progress
  }))
);
