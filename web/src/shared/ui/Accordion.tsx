import { useId, type ReactNode } from 'react';
import { Chevron } from './Chevron';

type AccordionProps = {
  title: string;
  open: boolean;
  onToggle: () => void;
  children: ReactNode;
  className: string;
};

export function Accordion({ title, open, onToggle, children, className }: AccordionProps) {
  const panelId = useId();
  const stateClassName = open ? ` ${className}--open` : '';

  return (
    <div className={`${className}-group`}>
      <button
        className={`${className}${stateClassName}`}
        type="button"
        aria-expanded={open}
        aria-controls={panelId}
        onClick={onToggle}
      >
        <span>{title}</span>
        <Chevron open={open} />
      </button>
      <div id={panelId} className={`${className}-panel`} hidden={!open}>
        {children}
      </div>
    </div>
  );
}
