;;; deft-config.el --- documentation -*- lexical-binding: t; -*-

;;; Commentary:
;;
;; 
;;

;;; Code:
(require 'package-manager-config)
(require 'markdown-config)

(defun my-deft/strip-front-matter (contents)
  (replace-regexp-in-string "^---\\(?:\n.*\\)*---.*$" "" contents))

(defun my-deft/parse-title-wrapper (f file contents)
  (let ((new-contents (my-deft/strip-front-matter contents)))
    (funcall f file new-contents)))

(defun my-deft/parse-summary-wrapper (f contents title)
  (let ((new-contents (my-deft/strip-front-matter contents)))
    (funcall f new-contents title)))

;; (advice-add 'deft-parse-title :around 'my-deft-parse-title-wrapper)
;; (advice-add 'deft-parse-summary :around 'my-deft-parse-summary-wrapper)
(use-package deft
  :ensure t
  :bind (("<f8>" . deft))
  :commands (deft deft-open-file deft-new-file-named)
  :config
  (advice-add 'deft-parse-title :around 'my-deft/parse-title-wrapper)
  (advice-add 'deft-parse-summary :around 'my-deft/parse-summary-wrapper)
  ;(advice-add 'deft-parse-title :around #'my-deft/parse-title-with-directory-prepended)
  (setq deft-directory "~/Notes/"
        deft-recursive t
        deft-extensions '("md" "txt" "org" "org.txt" "tex")
        deft-use-filter-string-for-filename nil
        deft-use-filename-as-title nil
        deft-markdown-mode-title-level 1
        deft-file-naming-rules '((noslash . "-")
                                 (nospace . "-")
                                 (case-fn . downcase))))

(provide 'deft-config)
;;; deft-config.el ends here
