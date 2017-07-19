
import dictionay = require('./dictionary');

class Translator {
  lang: string;

  constructor() {
    this.lang = "en";
  }

  translate(str:string): string {
    return (dictionay[this.lang] && dictionay[this.lang][str]) || str;
  }
}

export = Translator;
