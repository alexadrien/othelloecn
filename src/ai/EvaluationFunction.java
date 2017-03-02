package ai;

import othello.Othello;
import othello.Player;

/**
 *
 * @author Vincent
 */
public interface EvaluationFunction {
    public int evaluate(Othello game, Player p);
}
