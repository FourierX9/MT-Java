package org.mindtrails.domain.tracking;

import lombok.Data;
import org.apache.el.parser.BooleanNode;
import org.mindtrails.domain.data.DoNotDelete;
import org.mindtrails.domain.data.Exportable;
import org.mindtrails.domain.Participant;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * User: dan
 * Date: 7/24/14
 * Time: 9:33 AM
 * Logs the date and time a particular gift card was awarded.
 */
@Entity
@Table(name="gift_log")
@Exportable
@DoNotDelete
@Data
public class GiftLog extends MindTrailsLog {

    private String orderId;
    private String sessionName;
    private Date dateCreated = new Date();
    private int amount; // amount awarded in cents.

    public GiftLog() {};

    public GiftLog(Participant participant, String sessionName, int amount) {
        this.participant = participant;
        this.sessionName = sessionName;
        this.amount = amount;
    }

    public void markAwarded(String orderId) {
        this.orderId = orderId;
        this.dateSent = new Date();
    }
}
