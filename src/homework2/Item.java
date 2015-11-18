/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package homework2;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David
 */
public class Item implements Serializable {
    public String name;
    public double price;
    public Owner owner;
    
    @Override
    public String toString(){
        try {
            return name+", "+Double.toString(price)+", "+owner.getName();
        } catch (RemoteException ex) {
            Logger.getLogger(Item.class.getName()).log(Level.SEVERE, null, ex);
        }
        return name+", "+Double.toString(price)+", UNKNOWN";
    }
}
