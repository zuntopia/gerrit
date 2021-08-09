/**
 * @license
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {GrLitElement} from '../../lit/gr-lit-element';
import {css, customElement, html, property} from 'lit-element';

declare global {
  interface HTMLElementTagNameMap {
    'gr-settings-item': GrSettingsItem;
  }
}

@customElement('gr-settings-item')
export class GrSettingsItem extends GrLitElement {
  @property({type: String})
  anchor?: string;

  @property({type: String})
  title = '';

  static get styles() {
    return [
      css`
        :host {
          display: block;
          margin-bottom: var(--spacing-xxl);
        }
      `,
    ];
  }

  render() {
    const anchor = this.anchor ?? '';
    return html`<h2 id="${anchor}" class="heading-2">${this.title}</h2>`;
  }
}
