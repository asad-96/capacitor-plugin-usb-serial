import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NewLinePipe } from './pipes';

// https://github.com/danrevah/ngx-pipes
const modules: any[] = [FormsModule, ReactiveFormsModule, NewLinePipe];

@NgModule({
  declarations: [ NewLinePipe ],
  imports: [CommonModule],
  exports: [...modules],
})
export class SharedModule {}
