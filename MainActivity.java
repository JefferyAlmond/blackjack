package com.example.jeffery.blackjack;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    private int playerCash;
    private int playerBet;
    private boolean playing, playerStands, dealerStands, shoeMode;

    TextView totalCashTextView, playerBetTextView, playerScoreTextView, dealerScoreTextView, winnerTextView;

    Button betPlusButton, betMinusButton, shoeModeButton, hitButton, standButton, newGameButton;

    ImageView playerCard1, playerCard2, playerCard3, playerCard4, playerCard5;
    ImageView dealerCard1, dealerCard2, dealerCard3, dealerCard4, dealerCard5;

    ImageView playerCardImages[] = {playerCard1, playerCard2, playerCard3, playerCard4, playerCard5};
    ImageView dealerCardImages[] = {dealerCard1, dealerCard2, dealerCard3, dealerCard4, dealerCard5};

    hand playerCards;
    hand dealerCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playing = false;
        shoeMode = true;
        playerStands = false;
        dealerStands = false;
        playerCash = 1000; //starting cash
        playerBet = 100;

        totalCashTextView = (TextView) findViewById(R.id.totalCashText);
        playerBetTextView = (TextView) findViewById(R.id.currentBetText);
        playerScoreTextView = (TextView) findViewById(R.id.playerScoreText);
        dealerScoreTextView = (TextView) findViewById(R.id.dealerScoreText);
        winnerTextView = (TextView) findViewById(R.id.winnerTextView);

        betPlusButton = (Button) findViewById(R.id.betPlusButton);
        betMinusButton = (Button) findViewById(R.id.betMinusButton);
        shoeModeButton = (Button) findViewById(R.id.shoeModeButton);
        hitButton = (Button) findViewById(R.id.hitButton);
        standButton = (Button) findViewById(R.id.standButton);
        newGameButton = (Button) findViewById(R.id.newGameButton);

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

        newGame();
    }

    private void newGame() {

        playing = true;
        playerStands = false;
        dealerStands = false;

        playerCards = new hand();
        dealerCards = new hand();

        //these elements will be shown upon game completion
        winnerTextView.setVisibility(View.INVISIBLE);
        newGameButton.setVisibility(View.INVISIBLE);

        //set all player cards to blank
        for (ImageView playerCardImage : playerCardImages) {
            playerCardImage.setImageResource(R.drawable.blank);
            playerCardImage.setVisibility(View.INVISIBLE);
        }
        
        //set all dealer cards to blank
        for (ImageView dealerCardImage : dealerCardImages) {
            dealerCardImage.setImageResource(R.drawable.blank);
            dealerCardImage.setVisibility(View.INVISIBLE);
        }

        //blackjack default deals two cards to each player
        dealCard(playerCards);
        dealCard(dealerCards);
        dealCard(playerCards);
        dealCard(dealerCards);

        updateCards();
    }

    //update card images and player cash amount
    private void updateCards(){
        for (int i = 0; i < playerCards.cards.size(); i++){
            //construct a string based on value of card, retrieve image based on string name
            //there is probably a more elegant way of doing this, but this method functions
            String s = ("card_" + playerCards.cards.get(i).number + "_of_" + playerCards.cards.get(i).suit);
            int id = getResources().getIdentifier(s, "drawable", getPackageName());
            playerCardImages[i].setVisibility(View.VISIBLE);
            playerCardImages[i].setImageResource(id);
        }
        for (int i = 0; i < dealerCards.cards.size(); i++){
            String s = ("card_" + dealerCards.cards.get(i).number + "_of_" + dealerCards.cards.get(i).suit);
            int id = getResources().getIdentifier(s, "drawable", getPackageName());
            dealerCardImages[i].setVisibility(View.VISIBLE);
            dealerCardImages[i].setImageResource(id);
        }

        totalCashTextView.setText(getResources().getString(R.string.total_cash) + playerCash);
        playerBetTextView.setText(getResources().getString(R.string.current_bet) + playerBet);
        playerScoreTextView.setText(getResources().getString(R.string.player_score) + playerCards.score);
        dealerScoreTextView.setText(getResources().getString(R.string.dealer_score) + dealerCards.score);

    }

    //this section is largely unnecessary, as later versions of Android can recreate these methods with shorter XML statements
    //this was the method used in the tutorial I was following, however
    private void setButtonOnClickListeners() {

        betPlusButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!playing){
                    playerBet += 10; //10 is the default value based on starting cash of 1000 and default bet of 100
                    updateCards();
                }
            }
        });

        betMinusButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!playing){
                    if (playerBet != 0){ //only functions if bet is positive to prevent negative betting
                        playerBet -= 10;
                    }
                    updateCards();
                }
            }
        });

        //"shoe mode" allows duplicate cards to prevent card-counting, simulating casino "shoe" device 
        //cannot be toggled while game is in session
        shoeModeButton.setOnClickListener(new View.OnClickListener(){
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

        hitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (playing){ //button only active if game is active
                    dealCard(playerCards);
                    updateCards();
                    if (playerCards.score > 21){
                        if (playerCards.aces > 0){ //checks for aces. in case of score > 21, ace count is decremented by 1 and score by 10, to account for variable value of aces
                            playerCards.decAces();
                            playerCards.decScore();
                            updateCards();
                        }else{
                            dealerWins(); //if there are no aces left to decrement
                        }
                    }
                    if (playerCards.cards.size() >= 5 && playerCards.score <= 21){ //"five card charlie" rule always wins
                        playerWins();
                    }
                    dealerTurn();
                }
            }
        });

        standButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (playing){
                    playerStands = true;
                    if (dealerStands){ //final score comparison if both players stand
                        scoreCompare();
                    }
                    dealerTurn();
                }
            }
        });

        newGameButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                newGame();
            }
        });
    }

    //check both players for bust, then compares scores
    private void scoreCompare() {
        updateCards();
        if (playerCards.score > 21){    //player score checked first, since ties awarded to dealer
            dealerWins();
        }else if (dealerCards.score > 21){
            playerWins();
        }else if (playerCards.score > dealerCards.score){
            playerWins();
        }else{
            dealerWins();
        }
    }

    private void dealerWins() {
        playerCash -= playerBet;
        playing = false;
        updateCards();
        winnerTextView.setText(R.string.dealer_wins); //display dealer victory message
        winnerTextView.setVisibility(View.VISIBLE);
        newGameButton.setVisibility(View.VISIBLE);
    }

    private void playerWins(){
        playerCash += playerBet;
        playing = false;
        updateCards();
        winnerTextView.setText(R.string.player_wins); //display player victory message
        winnerTextView.setVisibility(View.VISIBLE);
        newGameButton.setVisibility(View.VISIBLE);
    }

    //assume dealer always hits on 16 or lower, otherwise stands
    private void dealerTurn(){
        if (playing){
            if (dealerCards.score < 17){
                dealCard(dealerCards);
                updateCards();
                if (dealerCards.score > 21){ //checks for aces. in case of score > 21, ace count is decremented by 1 and score by 10, to account for variable value of aces
                    if (dealerCards.aces > 0){
                        dealerCards.decAces();
                        dealerCards.decScore();
                        updateCards();
                    }else{
                        playerWins();
                    }
                }
                if (dealerCards.cards.size() >= 5 && dealerCards.score <= 21){ //"five card charlie" rule always wins
                    dealerWins();
                }
            }else{
                dealerStands = true;
                if (playerStands){
                    scoreCompare();
                }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //"card" consists of number and suit, plus getters and setters
    class card{
        private String suit;
        private String number;

        public card(){
            this.suit = null;
            this.number = null;
        }

        public void setSuit(String newSuit){
            this.suit = newSuit;
        }

        public String getSuit(){
            return suit;
        }

        public void setNumber(String newNumber){
            this.number = newNumber;
        }

        public String getNumber(){
            return number;
        }
    }

    //"hand" is a collection of 5 cards, aces tracked seperately to account for variable ace value
    class hand{
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
            this.score = this.score - 10; //score decreased by 10 to account for ace value of either 1 or 11
        }

    }

    //add a card to either player or dealer hand
    public void dealCard(hand hand){
        boolean repeatCheck = true;
        card nextCard = new card();

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

        if (nextCard.number.equals("ace")){
            hand.incAces(); //increase ace count if ace is dealt
        }

        hand.cards.add(nextCard);
        hand.incScore(faceValue(nextCard));
        updateCards();
    }

    //create a random card with 1 of 4 suits and 13 face values
    //"breaks" are supposed to be bad form, but greatly simplified this code
    public void generateCard(card nextCard){
        int dealSuit;
        int dealNumber;
        String checkSuit;
        String checkNumber;

        Random r = new Random();
        dealSuit = r.nextInt(4) + 1;
        
        //randomly choose from 1 of 4 suits
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

        //randomly choose from 1 of 13 face values
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

    //correlates card face value string to numerical value int
    //"100" was to detect errors
    public int faceValue(card card){
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
                return 11;
            default:
                return 100;
        }
    }

    //checks for repeat cards already in a hand. only active if shoe mode is on
    //check new card against all existing cards in both players' hands
    public boolean repeatCard(String number, String suit){
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
