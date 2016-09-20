/*
* A simple blackjack app. Goal is to get a higher score than the dealer, without going over 21.
* Ties go to dealer. Dealer always hits on a score of less than 17, stands otherwise.
* Natural blackjack (ace + face card) beats any other 21. Five card charlie beats any score.
* Double down on score of 9, 10, or 11 to take 1 more card and double bet.
* Surrender ends the game and gives up half your bet if things look hopeless.
* Shoe mode allows duplicate cards to show up.
* */

package com.jalmond.blackjack;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private int playerCash;
    private int playerBet;
    private boolean playing, playerStands, dealerStands, shoeMode;

    TextView totalCashTextView, playerBetTextView, playerScoreTextView, dealerScoreTextView, winnerTextView;

    Button betPlus10Button, betPlus100Button, shoeModeButton, hitButton, standButton, newGameButton, doubleDownButton, surrenderButton;

    ImageView playerCard1, playerCard2, playerCard3, playerCard4, playerCard5;
    ImageView dealerCard1, dealerCard2, dealerCard3, dealerCard4, dealerCard5;

    ImageView playerCardImages[] = {playerCard1, playerCard2, playerCard3, playerCard4, playerCard5};
    ImageView dealerCardImages[] = {dealerCard1, dealerCard2, dealerCard3, dealerCard4, dealerCard5};

    //"hand" object consists of 5 "card' objects, score, ace count
    hand playerCards;
    hand dealerCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //on initialization of app

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playing = false; //certain actions only available when "playing" is active
        shoeMode = true; //shoe mode allows for repeat cards. See method "repeatCard" for details
        playerStands = false;
        dealerStands = false;
        playerCash = 1000;  //default starting cash
        playerBet = 100;    //minimum bet

        totalCashTextView = (TextView) findViewById(R.id.totalCashText);
        playerBetTextView = (TextView) findViewById(R.id.currentBetText);
        playerScoreTextView = (TextView) findViewById(R.id.playerScoreText);
        dealerScoreTextView = (TextView) findViewById(R.id.dealerScoreText);
        winnerTextView = (TextView) findViewById(R.id.winnerTextView);

        betPlus10Button = (Button) findViewById(R.id.betPlus10Button);
        betPlus100Button = (Button) findViewById(R.id.betPlus100Button);
        shoeModeButton = (Button) findViewById(R.id.shoeModeButton);
        hitButton = (Button) findViewById(R.id.hitButton);
        standButton = (Button) findViewById(R.id.standButton);
        newGameButton = (Button) findViewById(R.id.newGameButton);
        doubleDownButton = (Button) findViewById(R.id.doubleDownButton);
        surrenderButton = (Button) findViewById(R.id.surrenderButton);

        //this could have been done in XML, initial tutorial was based on older android version
        setButtonOnClickListeners();

        playerCardImages[0] = (ImageView) findViewById(R.id.imagePlayerCard1);
        playerCardImages[1] = (ImageView) findViewById(R.id.imagePlayerCard2);
        playerCardImages[2] = (ImageView) findViewById(R.id.imagePlayerCard3);
        playerCardImages[3] = (ImageView) findViewById(R.id.imagePlayerCard4);
        playerCardImages[4] = (ImageView) findViewById(R.id.imagePlayerCard5);

        dealerCardImages[0] = (ImageView) findViewById(R.id.imageDealerCard1);
        dealerCardImages[1] = (ImageView) findViewById(R.id.imageDealerCard2);
        dealerCardImages[2] = (ImageView) findViewById(R.id.imageDealerCard3);
        dealerCardImages[3] = (ImageView) findViewById(R.id.imageDealerCard4);
        dealerCardImages[4] = (ImageView) findViewById(R.id.imageDealerCard5);

        //player total cash and current bet
        totalCashTextView.setText(getResources().getString(R.string.total_cash) + " " + playerCash);
        playerBetTextView.setText(getResources().getString(R.string.current_bet) + " " + playerBet);
        newGameButton.setVisibility(View.VISIBLE);
    }

    private void newGame() {
        //on new game selected
        playing = true;
        playerStands = false;
        dealerStands = false;
        //playerBet = 100;    //reset to minimum bet

        playerCards = new hand();
        dealerCards = new hand();

        winnerTextView.setVisibility(View.INVISIBLE);   //hide winner message
        newGameButton.setVisibility(View.INVISIBLE);    //hide New Game button
        dealerScoreTextView.setVisibility(View.INVISIBLE);  //hide dealer score until game over
        betPlus10Button.setVisibility(View.INVISIBLE);  //bet & shoe mode buttons cannot be used during play
        betPlus100Button.setVisibility(View.INVISIBLE);
        shoeModeButton.setVisibility(View.INVISIBLE);
        hitButton.setVisibility(View.VISIBLE); //hit, stand, double down, surrender unavailable outside play
        standButton.setVisibility(View.VISIBLE);
        doubleDownButton.setVisibility(View.VISIBLE);
        surrenderButton.setVisibility(View.VISIBLE);

        for (ImageView playerCardImage : playerCardImages) {    //set all player cards blank
            playerCardImage.setImageResource(R.drawable.blank);
            playerCardImage.setVisibility(View.INVISIBLE);
        }

        for (ImageView dealerCardImage : dealerCardImages) {    //set all dealer cards blank
            dealerCardImage.setImageResource(R.drawable.blank);
            dealerCardImage.setVisibility(View.INVISIBLE);
        }

        //first dealer card is hidden
        dealerCardImages[0].setImageResource(R.drawable.card_back);

        //deal 2 cards to each player
        dealCard(playerCards);
        dealCard(dealerCards);
        dealCard(playerCards);
        dealCard(dealerCards);

        //rarely-used code in the event two aces are dealt on the first hand
        if (playerCards.score > 21){
            playerCards.decAces();
            playerCards.decScore();
        }
        if (dealerCards.score > 21){
            dealerCards.decAces();
            dealerCards.decScore();
        }

        updateCards();
    }

    private void updateCards(){
        //refresh view of cards, scores, and update messages
        for (int i = 0; i < playerCards.cards.size(); i++){ //update all player card images
            //uses bitmapFactory to reduce card image size, as default card image sizes cause memory issues. how long did it take me to figure this out? don't ask.
            String s = ("card_" + playerCards.cards.get(i).number + "_of_" + playerCards.cards.get(i).suit);
            int id = getResources().getIdentifier(s, "drawable", getPackageName());
            int displayWidth = playerCardImages[i].getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; //access card image size without loading it into memoru
            BitmapFactory.decodeResource(getResources(), id, options);
            int imageWidth = options.outWidth;
            if (imageWidth > displayWidth){ //reduce size of card image based on size of display, saves memory
                int widthRatio = Math.round((float) imageWidth / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), id, options);
            playerCardImages[i].setVisibility(View.VISIBLE);
            playerCardImages[i].setImageBitmap(scaledBitmap);
        }
        for (int i = 1; i < dealerCards.cards.size(); i++){ //update all dealer card images EXCEPT first card - note starting value of i
            String s = ("card_" + dealerCards.cards.get(i).number + "_of_" + dealerCards.cards.get(i).suit);
            int id = getResources().getIdentifier(s, "drawable", getPackageName());
            int displayWidth = dealerCardImages[i].getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), id, options);
            int imageWidth = options.outWidth;
            if (imageWidth > displayWidth){
                int widthRatio = Math.round((float) imageWidth / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), id, options);
            dealerCardImages[i].setVisibility(View.VISIBLE);
            dealerCardImages[i].setImageBitmap(scaledBitmap);
        }

        //first dealer card is hidden
        dealerCardImages[0].setVisibility(View.VISIBLE);
        dealerCardImages[0].setImageResource(R.drawable.card_back);

        winnerTextView.setVisibility(View.INVISIBLE); //hide double down error message. make sure to call updateCards BEFORE setting the message!

        //update player cash and bet
        totalCashTextView.setText(getResources().getString(R.string.total_cash) + " " + playerCash);
        playerBetTextView.setText(getResources().getString(R.string.current_bet) + " " + playerBet);
        playerScoreTextView.setText(getResources().getString(R.string.player_score) + " " + playerCards.score);
        dealerScoreTextView.setText(getResources().getString(R.string.dealer_score) + " " + dealerCards.score);

        if (!playing){
            //display hidden dealer card if game is over
            String s = ("card_" + dealerCards.cards.get(0).number + "_of_" + dealerCards.cards.get(0).suit);
            int id = getResources().getIdentifier(s, "drawable", getPackageName());
            dealerCardImages[0].setVisibility(View.VISIBLE);
            dealerCardImages[0].setImageResource(id);
        }
    }

    private void setButtonOnClickListeners() {
        //set button listeners. this was done before I learned it could be done via XML but is being kept for posterity

        betPlus10Button.setOnClickListener(new View.OnClickListener() { //increase bet by 10, only available while game is active
            @Override
            public void onClick(View v) {
                //increase bet by 10 if game not active. can you increase your bet during the game in blackjack? I don't know.
                if (!playing && playerBet <= playerCash - 10) {
                    playerBet += 10;
                    playerBetTextView.setText(getResources().getString(R.string.current_bet) + " " + playerBet);
                }
            }
        });

        betPlus100Button.setOnClickListener(new View.OnClickListener(){ //increase bet by 100, only available while game is active
            @Override
            public void onClick(View v) {
                //increase bet by 100 if game not active
                if (!playing && playerBet <= playerCash-100){
                    playerBet += 100;
                    playerBetTextView.setText(getResources().getString(R.string.current_bet) + " " + playerBet);
                }
            }
        });

        shoeModeButton.setOnClickListener(new View.OnClickListener(){   //toggle shoe mode which allos repeat cards, only available while game is inactive
            @Override
            public void onClick(View v) {
                if (!playing){
                    if (shoeMode){
                        shoeMode = false;
                        shoeModeButton.setText(R.string.shoe_mode_off);
                    }else{
                        shoeMode = true;
                        shoeModeButton.setText(R.string.shoe_mode_on);
                    }
                }
            }
        });

        hitButton.setOnClickListener(new View.OnClickListener(){    //player hit
            @Override
            public void onClick(View v) {
                if (playing){
                    dealCard(playerCards);
                    updateCards();
                    //this code accounts for the variable value of aces. if the player goes over 21, the game checks to see if an ace is present, whose value can be changed from 11 to 1
                    if (playerCards.score > 21){
                        if (playerCards.aces > 0){
                            playerCards.decAces();
                            playerCards.decScore();
                            updateCards();
                        }else{
                            //no aces to decrement means player busts
                            dealerWins();
                        }
                    }
                    if (playerCards.cards.size() >= 5 && playerCards.score <= 21){
                        //five card charlie always wins
                        playerWins();
                    }
                    dealerTurn();
                }
            }
        });

        standButton.setOnClickListener(new View.OnClickListener(){  //player stands and passes turn to dealer
            @Override
            public void onClick(View v) {
                if (playing){
                    playerStands = true;
                    if (dealerStands){
                        scoreCompare();
                    }
                    dealerTurn();
                }
            }
        });

        newGameButton.setOnClickListener(new View.OnClickListener(){    //begins new game, only available upon win or loss
            @Override
            public void onClick(View v) {
                newGame();
            }
        });

        doubleDownButton.setOnClickListener(new View.OnClickListener(){    //double down gives player 1 more card and double bet
            @Override
            public void onClick(View v) {
                if (playing){
                    if (playerCards.score >= 9 && playerCards.score <=11){ //can only double down on 9, 10, 11
                        playerStands = true;
                        playerBet *= 2;   //double bet
                        dealCard(playerCards);
                        if (playerCards.score > 21){
                            if (playerCards.aces > 0){
                                playerCards.decAces();
                                playerCards.decScore();
                                updateCards();
                            }else{
                                //no aces to decrement means player busts
                                dealerWins();
                            }
                        }
                        if (playerCards.cards.size() >= 5 && playerCards.score <= 21){
                            //five card charlie always wins
                            playerWins();
                        }
                        dealerTurn();
                    }else{
                        winnerTextView.setVisibility(View.VISIBLE); //display error message
                        winnerTextView.setText(R.string.doubleDownError);
                    }
                }
            }
        });

        surrenderButton.setOnClickListener(new View.OnClickListener(){ //give up and only lose half bet
            @Override
            public void onClick(View v) {
                if(playing){
                    playerBet /= 2; //decrease bet by half
                    dealerWins();
                }
            }
        });
    }

    private void scoreCompare() {   //check to see whose score is higher if neither player has busted and both elect to stand
        updateCards();

        if (playerCards.score == 21 && dealerCards.score == 21){    //blackjack (21 with 2 cards) beats any other 21
            if (dealerCards.cards.size() == 2){
                dealerWins();
            }else if (playerCards.cards.size() == 2){
                playerWins();
            }
        }

        if (playerCards.score > 21){ //player busts
            dealerWins();
        }else if (dealerCards.score > 21){ //dealer busts
            playerWins();
        }else if (playerCards.score > dealerCards.score){ //ties awarded to dealer
            playerWins();
        }else{
            dealerWins();
        }
    }

    private void dealerWins() { //run when dealer declared winner
        playerCash -= playerBet; //player loses bet
        playing = false;
        updateCards();
        winnerTextView.setText(R.string.dealer_wins); //display loss message
        winnerTextView.setVisibility(View.VISIBLE);
        newGameButton.setVisibility(View.VISIBLE); //new game now available
        dealerScoreTextView.setVisibility(View.VISIBLE); //dealer score now visible
        playerBet = 100;    //reset to minimum bet

        betPlus10Button.setVisibility(View.VISIBLE);  //bet & shoe mode buttons can only be used between games
        betPlus100Button.setVisibility(View.VISIBLE);
        shoeModeButton.setVisibility(View.VISIBLE);
        hitButton.setVisibility(View.INVISIBLE); //hit, stand, double, surrender buttons cannot be used between games
        standButton.setVisibility(View.INVISIBLE);
        doubleDownButton.setVisibility(View.INVISIBLE);
        surrenderButton.setVisibility(View.INVISIBLE);
    }

    private void playerWins(){ //run when player declared winner
        playerCash += playerBet; //player wins bet
        playing = false;
        updateCards();
        winnerTextView.setText(R.string.player_wins); //display win message
        winnerTextView.setVisibility(View.VISIBLE);
        newGameButton.setVisibility(View.VISIBLE); //new game now available
        dealerScoreTextView.setVisibility(View.VISIBLE); //dealer score now visible
        playerBet = 100;    //reset to minimum bet

        betPlus10Button.setVisibility(View.VISIBLE);  //bet & shoe mode buttons can only be used between games
        betPlus100Button.setVisibility(View.VISIBLE);
        shoeModeButton.setVisibility(View.VISIBLE);
        hitButton.setVisibility(View.INVISIBLE); //hit, stand, double, surrender buttons cannot be used between games
        standButton.setVisibility(View.INVISIBLE);
        doubleDownButton.setVisibility(View.INVISIBLE);
        surrenderButton.setVisibility(View.INVISIBLE);
    }

    private void dealerTurn(){ //dealer action, method called after player action resolved
        if (playing){
            if (dealerCards.score < 17){ //dealer always hits on 16 or lower
                dealCard(dealerCards);
                updateCards();

                //this code accounts for the variable value of aces.
                // if the dealer goes over 21, the game checks to see if an ace is present, whose value can be changed from 11 to 1
                if (dealerCards.score > 21){
                    if (dealerCards.aces > 0){
                        dealerCards.decAces();
                        dealerCards.decScore();
                        updateCards();
                    }else{
                        playerWins();
                    }
                }

                //five card charlie always wins
                if (dealerCards.cards.size() >= 5 && dealerCards.score <= 21){
                    dealerWins();
                }
            }else{
                dealerStands = true;
                updateCards();
                winnerTextView.setText(R.string.dealer_stands); //display dealer stands message
                winnerTextView.setVisibility(View.VISIBLE);
                if (playerStands){
                    scoreCompare();
                }
            }

            //if the player stands, turn goes to dealer again until dealer stands or busts
            if (playerStands){
                dealerTurn();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.aboutBlackjack) {
            Intent intent = new Intent(this, InfoScreen.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    class card{
        //card has a suit and a number
        private String suit;
        private String number;

        public card(){
            this.suit = null;
            this.number = null;
        }

        public void setSuit(String newSuit){ //suit setter
            this.suit = newSuit;
        }

        public String getSuit(){
            return suit;
        } //suit getter

        public void setNumber(String newNumber){
            this.number = newNumber;
        } //number setter

        public String getNumber(){
            return number;
        } //number getter
    }

    class hand{
        //hand is an array of cards, keeps track of overall score and how many aces have not been decremented from 11 to 1
        private int aces = 0;
        private int score = 0;
        private ArrayList<card> cards = new ArrayList<>();

        public void incAces(){
            this.aces++;
        }

        public void decAces(){
            this.aces--;
        }

        public void incScore(int scorePlus){
            this.score += scorePlus;
        }

        public void decScore(){
            //score can only be decreased if an ace is changes from 11 to 1, thus decreasing score by 10
            this.score = this.score - 10;
        }

    }

    public void dealCard(hand hand){
        //add card to a hand,  adjust score and ace count
        boolean repeatCheck = true;
        card nextCard = new card();

        //check if selected card is already in either hand, reroll if it is
        if (!shoeMode){
            while (repeatCheck){
                generateCard(nextCard);
                if (!(repeatCard(nextCard.getNumber(), nextCard.getSuit()))){
                    repeatCheck = false;
                }
            }
        }else {
            generateCard(nextCard);
        }

        //add to aces if ace is dealt
        if (nextCard.number.equals("ace")){
            hand.incAces();
        }

        hand.cards.add(nextCard);
        hand.incScore(faceValue(nextCard));
        updateCards();
    }

    public void generateCard(card nextCard){
        //create 1 of 4 suits and 1 of 13 numbers
        int dealSuit;
        int dealNumber;
        String checkSuit;
        String checkNumber;

        Random r = new Random();
        dealSuit = r.nextInt(4) + 1;

        switch (dealSuit){
            case 1:
                checkSuit = "hearts";
                break;
            case 2:
                checkSuit = "clubs";
                break;
            case 3:
                checkSuit = "diamonds";
                break;
            default:
                checkSuit = "spades";
                break;
        }

        dealNumber = r.nextInt(13) + 1;

        switch (dealNumber){
            case 1:
                checkNumber = "ace";
                break;
            case 11:
                checkNumber = "jack";
                break;
            case 12:
                checkNumber = "queen";
                break;
            case 13:
                checkNumber = "king";
                break;
            default:
                checkNumber = Integer.toString(dealNumber);
                break;
        }

        nextCard.setSuit(checkSuit);
        nextCard.setNumber(checkNumber);
    }

    public int faceValue(card card){
        //determine int value for card number. probably could have saved code for numbers 2-10 somehow but this way works for all cards
        switch (card.number){
            case "2":
                return 2;
            case "3":
                return 3;
            case "4":
                return 4;
            case "5":
                return 5;
            case "6":
                return 6;
            case "7":
                return 7;
            case "8":
                return 8;
            case "9":
                return 9;
            case "10":
                return 10;
            case "jack":
                return 10;
            case "queen":
                return 10;
            case "king":
                return 10;
            case "ace":
                return 11;  //ace is worth 11 until it is decremented
            default:
                return 100; //detect errors in card generation
        }
    }

    public boolean repeatCard(String number, String suit){
        //check both hands to see if a card already exists
        //this code is never used if shoe mode is active
        for (int i = 0; i < playerCards.cards.size(); i++){
            if ((suit.equals(playerCards.cards.get(i).suit))&&(number.equals(playerCards.cards.get(i).number))){
                return true;
            }
        }
        for (int i = 0; i < dealerCards.cards.size(); i++){
            if ((suit.equals(dealerCards.cards.get(i).suit))&&(number.equals(dealerCards.cards.get(i).number))){
                return true;
            }
        }

        return false;
    }
}
