import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { analysisReducer } from './app/store/analysis.reducer';
import { AnalysisEffects } from './app/store/analysis.effects';
import {provideHttpClient} from '@angular/common/http';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideStore({ analysis: analysisReducer }),
    provideEffects([AnalysisEffects]),
    provideStoreDevtools(),
    provideHttpClient()
  ]
}).catch(err => console.error(err));
