import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';

import { analysisReducer } from './store/analysis.reducer';
import { AnalysisEffects } from './store/analysis.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideStore({ analysis: analysisReducer }),
    provideEffects([AnalysisEffects]),
    provideStoreDevtools()
  ]
};
