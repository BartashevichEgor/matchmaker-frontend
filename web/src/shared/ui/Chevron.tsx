type ChevronProps = {
  open?: boolean;
  className?: string;
};

export function Chevron({ open = false, className = 'accordion-chevron' }: ChevronProps) {
  const classes = [className, open ? `${className}--open` : ''].filter(Boolean).join(' ');

  return (
    <span className={classes} aria-hidden="true">
      <svg viewBox="0 0 24 24" focusable="false">
        <path d="m6 9 6 6 6-6" />
      </svg>
    </span>
  );
}
