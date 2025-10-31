import {inject, Injectable} from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ApiService } from '../core/services/api.service';
import * as AnalysisActions from './analysis.actions';
import { of } from 'rxjs';
import { catchError, map, mergeMap, switchMap } from 'rxjs/operators';

@Injectable()
export class AnalysisEffects {
  private actions$ = inject(Actions);
  private api = inject(ApiService);

  uploadFile$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AnalysisActions.uploadFile),
      mergeMap(({ file }) =>
        this.api.uploadAndAnalyze(file).pipe(
          map(() => AnalysisActions.uploadFileSuccess()),
          catchError(() =>
            of(AnalysisActions.uploadFileFailure({ error: 'Ошибка загрузки файла' }))
          )
        )
      )
    )
  );

  loadHistory$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AnalysisActions.loadHistory),
      switchMap(() =>
        this.api.getHistory().pipe(
          map(history => AnalysisActions.loadHistorySuccess({ history })),
          catchError(() =>
            of(AnalysisActions.loadHistoryFailure({ error: 'Ошибка загрузки истории' }))
          )
        )
      )
    );
  });

  loadDetail$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AnalysisActions.loadDetail),
      switchMap(({ id }) =>
        this.api.getDetail(id).pipe(
          map(detail => AnalysisActions.loadDetailSuccess({ detail })),
          catchError(() =>
            of(AnalysisActions.loadDetailFailure({ error: 'Ошибка загрузки деталей' }))
          )
        )
      )
    );
  });

  deleteAnalysis$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AnalysisActions.deleteAnalysis),
      mergeMap(({ id }) =>
        this.api.deleteAnalysis(id).pipe(
          map(() => AnalysisActions.deleteAnalysisSuccess({ id })),
          catchError(() =>
            of(AnalysisActions.deleteAnalysisFailure({ error: 'Ошибка удаления' }))
          )
        )
      )
    );
  });

  loadProgress$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AnalysisActions.loadProgress),
      switchMap(({ id }) =>
        this.api.getProgress(id).pipe(
          map(progress => AnalysisActions.loadProgressSuccess({ id, progress })),
          catchError(() =>
            of(AnalysisActions.loadProgressFailure({ error: 'Ошибка получения прогресса' }))
          )
        )
      )
    );
  });
}
