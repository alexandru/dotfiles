;;; my-functions.el --- -*- lexical-binding: t; -*-

;;; Commentary:
;;
;; My utilities go here.
;;

;;; Code:
(defun my/on-frame-execute (fn)
  (if (daemonp)
      (add-hook 'after-make-frame-functions
                (lambda (frame) (select-frame frame) (funcall fn)))
    ;; else
    (funcall fn)))

(provide 'my-functions)
;;; my-functions.el ends here
