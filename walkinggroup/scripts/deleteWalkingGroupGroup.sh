#!/bin/bash
curl -H 'apiKey: F369E8E6-244B-4672-B8A8-1E44A372CA496' -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjb2xlQHRlc3QuY29tIiwiZXhwIjoxNTM5NTU5NDI2fQ.wdtQX0o0NJRB1WCUxQzKZ7KEEt0LBuUqTL8YQT62dS3Tc_-z04p0NGfM2RAKsp4YdxHPy25z1XWTcDvLBXUGMg' https://cmpt276-1177-bf.cmpt.sfu.ca:8184/groups/$1 -X DELETE
