import { Component, inject } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  template: '<router-outlet />',
})
export class App {
  constructor() {
    // index.html'de yüklenen Material Symbols fontunu mat-icon için varsayılan yap.
    inject(MatIconRegistry).setDefaultFontSetClass('material-symbols-outlined');
  }
}
