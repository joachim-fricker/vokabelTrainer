# Gemeinsamer Import fuer die spanischen Lektionen (es-AR)
import os, sys
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'))
from lib import make as _make, check as _check

LANG = 'es-AR'

def make(*args, **kw):
    _make(*args, lang=LANG, **kw)

def check():
    _check(LANG)
