# Poker
Who doesn't like Poker? This project was created as a fun little side-project because I was bored. It is not complete yet, but
it works pretty well. There are a few cases where it will say you won, or you lost, when in actuality you didn't win or lose.
Also, there is no AI, so don't get a big head ;) 

![Poker](/Poker.png)

# How it works
The way this works is I just coded a simple GUI in Java AWT and Swing. This GUI places the cards at certain pixelc coordinates
etc etc. The way I determine who won is by using a set of rules to determine who won a certain round. These rules are not
complete, I was simply going based off the actual rules of Poker and tried to implement them the simplest way I could. 

The cards are froms some open source library I found online and I just put them all in a Spritesheet and pull the proper
image. The rest is pretty basic. I create a deck and pull random cards for each player. I also keep track of which cards have
been played so there are no duplicates.

# To run
In order to run this clone the repository to some spot on your computer. I was a little confused on how to run packages at first
so I'll let you know how it works. Make sure you have Java installed and your JAVA_HOME set in your environment variables.
Then open a Command Prompt or Terminal and change into the bin directory. Then run:
```
java main.Main
```
And it should run the game for you. 
