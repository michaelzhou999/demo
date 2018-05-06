#!/bin/bash

#
# A simple Linux Bash script to populate an arbitrary list of key/value pairs
# into the microservice, with value being upper case of key. 
#
# Start the microservice before running this seeding script.
# 

listOfKeys="one
two
three
four
five
six
seven
eight
nine
ten
eleven
twelve
thirteen
fourteen
fifteen
sixteen
seventeen
eighteen
ninteen
twenty
twenty-one
twenty-two
twenty-three
twenty-four
twenty-five
twenty-six
twenty-seven
twenty-eight
twenty-nine
thirty
thirty-one
thirty-two"

for lower in $listOfKeys
do
    upper=`echo "$lower" | tr [a-z] [A-Z]`
    curl -i -X POST -H 'content-type:application/json' -d "{\"key\":\"$lower\",\"value\":\"$upper\"}" http://localhost:8080/mappings
done

