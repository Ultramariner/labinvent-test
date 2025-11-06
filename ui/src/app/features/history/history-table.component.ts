import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import * as AnalysisActions from '../../store/analysis.actions';
import * as AnalysisSelectors from '../../store/analysis.selectors';
import { HistoryItem } from '../../core/models/history-item.model';
import { AppState } from '../../store/analysis.models';

import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';

@Component({
  selector: 'app-history-table',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatCardModule,
    MatProgressBarModule,
    MatSelectModule,
    MatOptionModule
  ],
  templateUrl: './history-table.component.html',
  styleUrls: ['./history-table.component.scss']
})
export class HistoryTableComponent implements OnInit {
  history$!: Observable<HistoryItem[]>;
  pagedHistory$!: Observable<HistoryItem[]>;
  totalPages$!: Observable<number>;

  loading$!: Observable<boolean>;
  error$!: Observable<string | undefined>;

  readonly AnalysisSelectors = AnalysisSelectors;

  currentPage = 1;
  pageSize = 5;

  displayedColumns = [
    'fileName',
    'fileSize',
    'duration',
    'avg',
    'stdDev',
    'status',
    'progress',
    'actions'
  ];

  constructor(private store: Store<AppState>) {}

  ngOnInit() {
    const savedSize = localStorage.getItem('pageSize');
    if (savedSize) {
      this.pageSize = +savedSize;
    }

    this.history$ = this.store.pipe(select(AnalysisSelectors.selectHistory));
    this.loading$ = this.store.pipe(select(AnalysisSelectors.selectHistoryLoading));
    this.error$ = this.store.pipe(select(AnalysisSelectors.selectHistoryError));

    this.pagedHistory$ = this.history$.pipe(
      map(history => {
        const start = (this.currentPage - 1) * this.pageSize;
        return history.slice(start, start + this.pageSize);
      })
    );

    this.totalPages$ = this.history$.pipe(
      map(history => Math.max(1, Math.ceil(history.length / this.pageSize)))
    );

    this.store.dispatch(AnalysisActions.loadHistory());
  }

  prevPage(totalPages: number) {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.recalcPage();
    }
  }

  nextPage(totalPages: number) {
    if (this.currentPage < totalPages) {
      this.currentPage++;
      this.recalcPage();
    }
  }

  private recalcPage() {
    this.pagedHistory$ = this.history$.pipe(
      map(history => {
        const start = (this.currentPage - 1) * this.pageSize;
        return history.slice(start, start + this.pageSize);
      })
    );
  }

  private recalcTotalPages() {
    this.totalPages$ = this.history$.pipe(
      map(history => Math.max(1, Math.ceil(history.length / this.pageSize)))
    );
  }

  delete(id: number) {
    this.store.dispatch(AnalysisActions.deleteAnalysis({ id }));
  }

  progress$(id: number) {
    return this.store.select(AnalysisSelectors.selectProgress(id));
  }

  cancel(id: number) {
    this.store.dispatch(AnalysisActions.cancelAnalysis({ id }));
  }

  restart(id: number) {
    this.store.dispatch(AnalysisActions.restartAnalysis({ id }));
  }

  onPageSizeChange(newSize: number) {
    this.pageSize = newSize;
    this.currentPage = 1;
    localStorage.setItem('pageSize', String(newSize));
    this.recalcPage();
    this.recalcTotalPages();
  }
}
