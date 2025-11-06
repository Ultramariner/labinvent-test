import { Component, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { Actions, ofType } from '@ngrx/effects';

import * as AnalysisActions from '../../store/analysis.actions';
import * as AnalysisSelectors from '../../store/analysis.selectors';
import { AppState } from '../../store/analysis.models';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-upload-form',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatInputModule, MatSnackBarModule],
  templateUrl: './upload-form.component.html',
  styleUrls: ['./upload-form.component.scss']
})
export class UploadFormComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  selectedFile: File | null = null;

  loading$!: Observable<boolean>;
  error$!: Observable<string | undefined>;

  constructor(private store: Store<AppState>, private actions$: Actions, private snackBar: MatSnackBar) {
    this.loading$ = this.store.pipe(select(AnalysisSelectors.selectUploadLoading));
    this.error$ = this.store.pipe(select(AnalysisSelectors.selectUploadError));

    this.actions$.pipe(ofType(AnalysisActions.uploadFileSuccess)).subscribe(() => {
      this.snackBar.open('Файл загружен', undefined, { duration: 3000 });
      this.selectedFile = null;
      if (this.fileInput) this.fileInput.nativeElement.value = '';
    });

    this.error$.subscribe(err => {
      if (err) this.snackBar.open(err, undefined, { duration: 4000, panelClass: 'error-snackbar' });
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    if (file.type !== 'text/csv') {
      this.snackBar.open('Можно загружать только CSV файлы', undefined, { duration: 3000 });
      this.selectedFile = null;
      input.value = '';
      return;
    }
    if (file.size > 50 * 1024 * 1024) {
      this.snackBar.open('Файл слишком большой (максимум 50 МБ)', undefined, { duration: 3000 });
      this.selectedFile = null;
      input.value = '';
      return;
    }

    this.selectedFile = file;
    this.snackBar.open(`Выбран файл: ${file.name}`, undefined, { duration: 3000 });
  }

  onUpload() {
    if (!this.selectedFile) {
      this.snackBar.open('Сначала выберите файл', undefined, { duration: 3000 });
      return;
    }
    this.store.dispatch(AnalysisActions.uploadFile({ file: this.selectedFile }));
    this.selectedFile = null;
  }
}
