#!/usr/bin/python
# -*- coding: utf-8 -*-

import json
import pytz
from datetime import datetime

jsonFile=open('sample2.txt', 'r')
values = json.load(jsonFile)
jsonFile.close()
fmt = '%Y-%m-%dT%H:%M:%SZ'

def func(values):
        lst=[]
        for value in values:
                d={}
                d["id"]=value["id"]
                temp = datetime.strptime(value["created_at"],'%a %b %d %H:%M:%S +0000 %Y').replace(tzinfo=pytz.UTC)
                d["created_at"]=temp.strftime(fmt)
                d["lang"]=value["lang"]
                d["text"]=value["text"]
                if value["entities"]["urls"]:
                        d["tweet_urls"]=value["entities"]["urls"][0]["expanded_url"]
                if not value["entities"]["urls"]:
                        d["tweet_urls"]=[]
                if value["entities"]["hashtags"]:
                        d["tweet_hashtags"]=value["entities"]["hashtags"][0]["text"]
                if not value["entities"]["hashtags"]:
                        d["tweet_hashtags"]=[]
                lst.append(d)
        return json.dumps(lst)

print func(values)
