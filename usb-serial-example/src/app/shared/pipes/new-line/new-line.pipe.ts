import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'newLine'
})
export class NewLinePipe implements PipeTransform {

  transform(value: string): string {
    if (!value) {
      return;
    }
    return value.replace(/(?:\r\n|\r|\n)/g, '<br>');
  }

}
