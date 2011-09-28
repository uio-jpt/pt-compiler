if exists("did_load_filetypes")
    finish
endif
augroup filetypedetect
    au BufRead,BufNewFile *.ptjava setfiletype javapt
    au BufRead,BufNewFile *.javapt setfiletype javapt
    au BufRead,BufNewFile *.jpt setfiletype javapt
augroup END
