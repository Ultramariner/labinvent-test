import { Routes } from '@angular/router';
import { UploadFormComponent } from './features/upload/upload-form.component';
import { HistoryTableComponent } from './features/history/history-table.component';
import { HistoryDetailComponent } from './features/history/history-detail.component';

export const routes: Routes = [
  { path: '', redirectTo: 'upload', pathMatch: 'full' },
  { path: 'upload', component: UploadFormComponent },
  { path: 'history', component: HistoryTableComponent },
  { path: 'history/:id', component: HistoryDetailComponent }
];
