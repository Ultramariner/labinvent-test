import { Component } from '@angular/core';
import { ApiService } from './services/api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html'
})
export class AppComponent {
  message = 'Hello';

  constructor(private api: ApiService) {}

  callApi() {
    this.api.test().subscribe({
      next: (res) => this.message = res,
      error: (err) => this.message = 'Ошибка: ' + err.message
    });
  }
}
