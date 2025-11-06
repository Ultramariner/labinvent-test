import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Subject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AnalysisEvent {
  id: number;
  status: string;
  progress?: number;
}

@Injectable({ providedIn: 'root' })
export class AnalysisWsService {
  private client: Client;
  private events$ = new Subject<AnalysisEvent>();

  constructor() {
    this.client = new Client({
      brokerURL: environment.apiUrl.replace(/^http/, 'ws') + '/ws',
      reconnectDelay: 5000,
      debug: (str: string) => console.log('[STOMP]', str),
    });

    this.client.onConnect = () => {
      console.log('STOMP connected');
      this.client.subscribe('/topic/analysis', (message: IMessage) => {
        try {
          const event: AnalysisEvent = JSON.parse(message.body);
          this.events$.next(event);
        } catch (e) {
          console.error('Ошибка парсинга WS-сообщения', e, message.body);
        }
      });
    };

    this.client.onStompError = frame => {
      console.error('Broker error:', frame.headers['message'], frame.body);
    };

    this.client.activate();
  }

  events(): Observable<AnalysisEvent> {
    return this.events$.asObservable();
  }
}
