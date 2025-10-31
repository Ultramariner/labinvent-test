import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import * as AnalysisActions from '../../store/analysis.actions';
import * as AnalysisSelectors from '../../store/analysis.selectors';
import { HistoryItem } from '../../core/models/history-item.model';
import { NotificationComponent } from '../../shared/notification.component';
import { AppState } from '../../store/analysis.models';

@Component({
  selector: 'app-history-table',
  standalone: true,
  imports: [CommonModule, RouterLink, NotificationComponent],
  templateUrl: './history-table.component.html',
  styleUrls: ['./history-table.component.scss']
})
export class HistoryTableComponent implements OnInit {
  history$!: Observable<HistoryItem[]>;
  loading$!: Observable<boolean>;
  error$!: Observable<string | undefined>;

  readonly AnalysisSelectors = AnalysisSelectors;

  constructor(private store: Store<AppState>) {}

  ngOnInit() {
    this.history$ = this.store.pipe(select(AnalysisSelectors.selectHistory));
    this.loading$ = this.store.pipe(select(AnalysisSelectors.selectHistoryLoading));
    this.error$ = this.store.pipe(select(AnalysisSelectors.selectHistoryError));

    this.store.dispatch(AnalysisActions.loadHistory());
  }

  delete(id: number) {
    this.store.dispatch(AnalysisActions.deleteAnalysis({ id }));
  }

  progress$(id: number) {
    return this.store.select(AnalysisSelectors.selectProgress(id));
  }
}
