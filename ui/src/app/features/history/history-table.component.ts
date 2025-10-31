import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { select, Store } from '@ngrx/store';
import { Observable, Subscription, interval } from 'rxjs';

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

  private pollingSub?: Subscription;

  constructor(private store: Store<AppState>) {}

  ngOnInit() {
    this.history$ = this.store.pipe(select(AnalysisSelectors.selectHistory));
    this.loading$ = this.store.pipe(select(AnalysisSelectors.selectHistoryLoading));
    this.error$ = this.store.pipe(select(AnalysisSelectors.selectHistoryError));

    this.store.dispatch(AnalysisActions.loadHistory());

    this.pollingSub = interval(1000).subscribe(() => {
      this.history$.subscribe(history => {
        history
          .filter(item => item.status === 'PROCESSING')
          .forEach(item => {
            this.store.dispatch(AnalysisActions.loadProgress({id: item.id}));
          });
      });
    });
  }

  ngOnDestroy() {
    this.pollingSub?.unsubscribe();
  }

  delete(id: number) {
    this.store.dispatch(AnalysisActions.deleteAnalysis({ id }));
  }
}
