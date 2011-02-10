#!/bin/bash
find *.dia -print0 | xargs -n 1 -0 dia -t eps-pango
find *.eps -print0 | xargs -n 1 -0 epstopdf
