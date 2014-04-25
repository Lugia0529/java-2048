/*
 * Copyright (c) 2014 Lugia Programming Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class Main extends JFrame implements KeyListener
{
    /* CLASS MEMBER */
    private Random mRand;

    private JLabel mScoreLabel;
    private JLabel mMoveLabel;

    private int mMove;
    private int mScore;
    private int mEmptyTileCount;

    /* ARRAY MEMBER */
    private JLabel[/* row */][/* col */] mTileLabel;

    private int[/* row */][/* col */] mTileValues;

    /* CONSTANT */
    public static final int ACTION_UP    = 0;
    public static final int ACTION_DOWN  = 1;
    public static final int ACTION_LEFT  = 2;
    public static final int ACTION_RIGHT = 3;

    public Main()
    {
        super("2048");

        initUI();
        initGame();
    }

    // =============================================
    //  INTERNAL FUNCTION
    // =============================================

    private void initUI()
    {
        setLayout(new BorderLayout(8, 8));

        JPanel mainPanel  = new JPanel(new BorderLayout(8, 8));
        JPanel scorePanel = new JPanel(new GridLayout(1, 2, 8, 8));
        JPanel tilePanel  = new JPanel(new GridLayout(4, 4, 8, 8));

        // tile panel
        mTileLabel = new JLabel[4][4];

        for (int row = 0; row < 4; row++)
        {
            for (int col = 0; col < 4; col++)
            {
                mTileLabel[row][col] = new JLabel("", JLabel.CENTER);
                mTileLabel[row][col].setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
                tilePanel.add(mTileLabel[row][col]);
            }
        }

        // score panel
        mMoveLabel = new JLabel("", JLabel.CENTER);
        mMoveLabel.setBorder(BorderFactory.createTitledBorder(new TitledBorder("Move")));

        mScoreLabel = new JLabel("", JLabel.CENTER);
        mScoreLabel.setBorder(BorderFactory.createTitledBorder(new TitledBorder("Score")));

        scorePanel.add(mMoveLabel);
        scorePanel.add(mScoreLabel);

        // main panel
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        mainPanel.add(scorePanel, BorderLayout.NORTH);
        mainPanel.add(tilePanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());

        add(mainPanel);

        addKeyListener(Main.this);

        setSize(300, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void initGame()
    {
        // random generator to determine where to spawn next number
        mRand = new Random(System.currentTimeMillis());

        // use to record current empty tile number
        mEmptyTileCount = 16;

        // use to record user current score and move count
        mScore = 0;
        mMove  = 0;

        // use to record current value of each tile
        mTileValues = new int[][]
        {
            { 0, 0, 0, 0 },
            { 0, 0, 0, 0 },
            { 0, 0, 0, 0 },
            { 0, 0, 0, 0 }
        };

        // randomly spawning 2 number
        addNewNumber();
        addNewNumber();

        // update UI
        updateTile();
        updateScore();
    }

    private void addNewNumber()
    {
        // check for game over
        if (mEmptyTileCount == 0)
            return;

        // this is really funny algorithm to determine which tile to be spawn next...
        // but who cares? i think everyone does happy as long as the algorithm work...
        int tileToSkip = mRand.nextInt(mEmptyTileCount--);

        // random generator will first generate a number between 0 to current empty tiles count
        // the generated number treated as the number of empty tile to skip before spawning new number
        for (int row = 0; row < 4; row++)
        {
            for (int col = 0; col < 4; col++)
            {
                // we only care about empty tiles
                if (mTileValues[row][col] != 0)
                    continue;

                // keep skipping until tileToSkip reached 0
                if (tileToSkip == 0)
                {
                    System.out.printf("Next Tile: %d\n", (row * 4) + col);

                    mTileValues[row][col] = 2;

                    return;
                }
                else
                    tileToSkip--;
            }
        }
    }

    private void doAction(int action)
    {
        // user make move, increase move count
        mMove++;

        // generally, we need two nested loops for this shit work
        // since another loops run in reverse order, we combine them into one

        // we have to do the for loops in reverse order for the down and right action
        boolean doInReverse = (action == ACTION_DOWN || action == ACTION_RIGHT);

        // start spamming for loops
        for (int row = doInReverse ? 3 : 0; doInReverse ? row >= 0 : row < 4; row += doInReverse ? -1 : 1)
        {
            for (int col = doInReverse ? 3 : 0; doInReverse ? col >= 0 : col < 4; col += doInReverse ? -1 : 1)
            {
                // skip empty tile
                if (mTileValues[row][col] == 0)
                    continue;

                System.out.printf("Try moving tile %d\n", (row * 4) + col);

                // the first for loops will try to match any possible tile
                // the second for loops will move all tile toward the direction

                switch (action)
                {
                    case ACTION_UP:
                    {
                        for (int i = row + 1; i < 4 && !matchTile(row, col, i, col); i++);

                        for (int i = row; i > 0 && mTileValues[i - 1][col] == 0 && moveTile(i - 1, col, i, col); i--);

                        break;
                    }

                    case ACTION_LEFT:
                    {
                        for (int i = col + 1; i < 4 && !matchTile(row, col, row, i); i++);

                        for (int i = col; i > 0 && mTileValues[row][i - 1] == 0 && moveTile(row, i - 1, row, i); i--);

                        break;
                    }

                    case ACTION_DOWN:
                    {
                        for (int i = row - 1; i >= 0 && !matchTile(row, col, i, col); i--);

                        for (int i = row; i < 3 && mTileValues[i + 1][col] == 0 && moveTile(i + 1, col, i, col); i++);

                        break;
                    }

                    case ACTION_RIGHT:
                    {
                        for (int i = col - 1; i >= 0 && !matchTile(row, col, row, i); i--);

                        for (int i = col; i < 3 && mTileValues[row][i + 1] == 0 && moveTile(row, i + 1, row, i); i++);
                    }
                }
            }
        }
    }

    private boolean moveTile(int row1, int col1, int row2, int col2)
    {
        System.out.printf("Move tile %d to %d\n", (row1 * 4) + col1,  (row2 * 4) + col2);

        mTileValues[row1][col1] = mTileValues[row2][col2];
        mTileValues[row2][col2] = 0;

        outputArray();

        return true;
    }

    private boolean matchTile(int row1, int col1, int row2, int col2)
    {
        // ignore empty tile
        if (mTileValues[row1][col1] == 0 || mTileValues[row2][col2] == 0)
            return false;

        // ignore mismatch
        if (mTileValues[row1][col1] != mTileValues[row2][col2])
            return true;

        System.out.printf("Match tile %d(%d) and %d(%d)\n", (row1 * 4) + col1, mTileValues[row1][col1], (row2 * 4) + col2, mTileValues[row2][col2]);

        // multiplying the value by 2
        mTileValues[row1][col1] *= 2;
        mTileValues[row2][col2] = 0;

        // update score
        mScore += mTileValues[row1][col1];

        System.out.printf("Update Score to %d (+%d)\n", mScore, mTileValues[row1][col1]);

        // a tile is removed after matching
        mEmptyTileCount++;

        return true;
    }

    private boolean isFull()
    {
        return mEmptyTileCount == 0;
    }

    private boolean checkForPossibleMove()
    {
        // function to determine whether there is any possible move
        // this function is only called when the tile is fully occupied
        for (int row = 0; row < 4; row++)
        {
            for (int col = 0; col < 4; col++)
            {
                // just do the simple matching with sibling
                if (row > 0 && mTileValues[row][col] == mTileValues[row - 1][col]) return true; // up
                if (row < 3 && mTileValues[row][col] == mTileValues[row + 1][col]) return true; // down
                if (col > 0 && mTileValues[row][col] == mTileValues[row][col - 1]) return true; // left
                if (col < 3 && mTileValues[row][col] == mTileValues[row][col + 1]) return true; // right
            }
        }

        return false;
    }

    private void updateTile()
    {
        for (int row = 0; row < 4; row++)
            for (int col = 0; col < 4; col++)
                mTileLabel[row][col].setText(mTileValues[row][col] != 0 ? String.valueOf(mTileValues[row][col]) : "");
    }

    private void updateScore()
    {
        mScoreLabel.setText(String.valueOf(mScore));
        mMoveLabel.setText(String.valueOf(mMove));
    }

    private void outputArray()
    {
        for (int row = 0; row < 4; row++)
        {
            for (int col = 0; col < 4; col++)
                System.out.print(mTileValues[row][col] + " ");

            System.out.println();
        }
    }

    // =============================================
    //  EVENT HANDLER
    // =============================================

    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_UP:
                System.out.println("Key Pressed: UP");

                doAction(ACTION_UP);

                break;

            case KeyEvent.VK_DOWN:
                System.out.println("Key Pressed: DOWN");

                doAction(ACTION_DOWN);

                break;

            case KeyEvent.VK_LEFT:
                System.out.println("Key Pressed: LEFT");

                doAction(ACTION_LEFT);

                break;

            case KeyEvent.VK_RIGHT:
                System.out.println("Key Pressed: RIGHT");

                doAction(ACTION_RIGHT);

                break;

            default:
                System.out.println("non-implementation key pressed, ignore.");
        }

        addNewNumber();

        // update the UI
        updateTile();
        updateScore();

        // check for game over
        if (isFull() && !checkForPossibleMove())
        {
            String result = String.format("Game Over! \nMove: %d \nScore: %d", mMove, mScore);

            JOptionPane.showMessageDialog(Main.this, result, "GameOver", JOptionPane.INFORMATION_MESSAGE);

            System.out.println("Game Over, reset game");

            initGame();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { /* No Implementation */ }

    @Override
    public void keyReleased(KeyEvent e) { /* No Implementation */ }

    // =============================================
    //  MAIN FUNCTION
    // =============================================

    public static void main(String... args)
    {
        // make the UI same as the system
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); }

        Main frame = new Main();

        frame.setVisible(true);
    }
}
