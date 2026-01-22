import * as React from "react"
import { cn } from "@/lib/utils"

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "default" | "outline" | "ghost" | "link"
  size?: "default" | "sm" | "lg" | "icon"
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "default", size = "default", ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(
          "inline-flex items-center justify-center rounded-full font-medium transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none ring-offset-background",
          {
            "bg-primary text-white hover:bg-white hover:text-primary shadow-lg shadow-primary/30 hover:shadow-xl hover:shadow-white/20": variant === "default",
            "border border-white/20 bg-white/5 hover:bg-white/10 text-white backdrop-blur-sm": variant === "outline",
            "hover:bg-white/10 text-white": variant === "ghost",
            "text-primary underline-offset-4 hover:underline": variant === "link",
            "h-10 py-2 px-6 text-sm": size === "default",
            "h-9 px-4 text-xs": size === "sm",
            "h-14 px-8 text-base font-semibold": size === "lg",
            "h-10 w-10": size === "icon",
          },
          className
        )}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button }
