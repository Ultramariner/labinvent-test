import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import * as AnalysisActions from '../../store/analysis.actions';
import * as AnalysisSelectors from '../../store/analysis.selectors';
import { AnalysisDetail } from '../../core/models/analysis-detail.model';
import { NotificationComponent } from '../../shared/notification.component';
import { AppState } from '../../store/analysis.models';

@Component({
  selector: 'app-history-detail',
  standalone: true,
  imports: [CommonModule, NotificationComponent],
  templateUrl: './history-detail.component.html',
  styleUrls: ['./history-detail.component.scss']
})
export class HistoryDetailComponent implements OnInit {
  detail$!: Observable<AnalysisDetail | null>;
  loading$!: Observable<boolean>;
  error$!: Observable<string | undefined>;

  constructor(private route: ActivatedRoute, private store: Store<AppState>, private router: Router) {
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.store.dispatch(AnalysisActions.loadDetail({ id }));
    }

    this.detail$ = this.store.pipe(select(AnalysisSelectors.selectDetail));
    this.loading$ = this.store.pipe(select(AnalysisSelectors.selectDetailLoading));
    this.error$ = this.store.pipe(select(AnalysisSelectors.selectDetailError));
  }

  goBack() {
    this.router.navigate(['/history']); // путь на список истории
  }
}
