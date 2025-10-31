import {Component, ViewChild, ElementRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import * as AnalysisActions from '../../store/analysis.actions';
import * as AnalysisSelectors from '../../store/analysis.selectors';
import { NotificationComponent } from '../../shared/notification.component';
import { AppState } from '../../store/analysis.models';
import {Actions, ofType} from '@ngrx/effects';

@Component({
  selector: 'app-upload-form',
  standalone: true,
  imports: [CommonModule, NotificationComponent],
  templateUrl: './upload-form.component.html',
  styleUrls: ['./upload-form.component.scss']
})
export class UploadFormComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  selectedFile: File | null = null;
  message = '';

  loading$!: Observable<boolean>;
  error$!: Observable<string | undefined>;

  constructor(private store: Store<AppState>, private actions$: Actions) {
    this.loading$ = this.store.pipe(select(AnalysisSelectors.selectUploadLoading));
    this.error$ = this.store.pipe(select(AnalysisSelectors.selectUploadError));

    this.actions$.pipe(ofType(AnalysisActions.uploadFileSuccess)).subscribe(() => {
      this.message = 'Файл загружен';
      this.selectedFile = null;
      if (this.fileInput) {
        this.fileInput.nativeElement.value = '';
      }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    if (file.type !== 'text/csv') {
      this.message = 'Можно загружать только CSV файлы';
      this.selectedFile = null;
      input.value = '';
      return;
    }
    if (file.size > 50 * 1024 * 1024) {
      this.message = 'Файл слишком большой (максимум 50 МБ)';
      this.selectedFile = null;
      return;
    }

    this.selectedFile = file;
    this.message = `Выбран файл: ${file.name}`;
  }

  onUpload() {
    if (!this.selectedFile) {
      this.message = 'Сначала выберите файл';
      return;
    }

    this.store.dispatch(AnalysisActions.uploadFile({ file: this.selectedFile }));
    this.selectedFile = null;
    this.message = '';
  }
}
