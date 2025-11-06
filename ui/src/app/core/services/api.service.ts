import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HistoryItem } from '../models/history-item.model';
import { AnalysisDetail } from '../models/analysis-detail.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {
  }

  uploadAndAnalyze(file: File): Observable<void> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post<void>(`${this.baseUrl}/analyze`, formData);
  }

  startAnalysis(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/analyze/${id}/start`, {});
  }

  cancelAnalysis(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/analyze/${id}/cancel`, {});
  }

  getProgress(id: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/analyze/${id}/progress`);
  }

  getHistory(): Observable<HistoryItem[]> {
    return this.http.get<HistoryItem[]>(`${this.baseUrl}/history`);
  }

  getDetail(id: number): Observable<AnalysisDetail> {
    return this.http.get<AnalysisDetail>(`${this.baseUrl}/history/${id}`);
  }

  deleteAnalysis(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/history/${id}`);
  }
}
