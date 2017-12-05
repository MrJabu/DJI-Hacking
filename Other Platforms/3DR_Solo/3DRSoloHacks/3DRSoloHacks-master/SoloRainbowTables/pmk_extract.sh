for each in `cat SSIDs.txt`; do echo $each; airolib-ng SoloLinkDB --export cowpatty $each $each.pmk; done
