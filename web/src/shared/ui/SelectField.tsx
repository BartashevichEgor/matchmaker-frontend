import type { SelectHTMLAttributes } from 'react';
import { Chevron } from './Chevron';

type SelectFieldProps = SelectHTMLAttributes<HTMLSelectElement>;

export function SelectField({ children, ...props }: SelectFieldProps) {
  return (
    <span className="select-field-control">
      <select {...props}>{children}</select>
      <Chevron className="select-field-chevron" />
    </span>
  );
}
