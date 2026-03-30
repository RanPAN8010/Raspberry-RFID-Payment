package Raspberry.service;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;

/**
 * Service de gestion des paiements et des recharges.
 * Cette classe contient la logique métier pour traiter les transactions financières.
 */
public class PaymentService {
    private UserDAO userDAO;
    
    /**
     * Constructeur de PaymentService.
     * Initialise l'objet d'accès aux données utilisateur (UserDAO).
     */
    public PaymentService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Traite une demande de paiement (Fonctionnalité Marchand).
     *
     * @param rfidTag Le tag RFID de l'utilisateur effectuant le paiement.
     * @param amount  Le montant à débiter.
     * @return true si le paiement est réussi, sinon false.
     */
    public boolean processPayment(String rfidTag, double amount) {
        User user = userDAO.getUserByRfid(rfidTag);

        if (user == null) {
            System.out.println("Échec du paiement : aucun compte trouvé pour ce RFID !");
            return false;
        }
        
        if (!user.isActive()) {
            System.out.println("Échec du paiement : ce compte a été désactivé !");
            return false;
        }

        if (user.getBalance() < amount) {
            System.out.println("Échec du paiement : solde insuffisant ! Solde actuel : €" + user.getBalance());
            return false;
        }

        // 扣款逻辑
        double newBalance = user.getBalance() - amount;
        if (userDAO.updateBalance(rfidTag, newBalance)) {
            System.out.println("Paiement réussi ! Débité : €" + amount + ", Solde restant : €" + newBalance);
            userDAO.addTransaction(rfidTag, "PAIEMENT", amount);
            return true;
        } else {
            System.out.println("Échec du paiement : erreur système.");
            return false;
        }
    }
    
    /**
     * Traite une demande de recharge (Fonctionnalité Utilisateur/Administrateur).
     *
     * @param rfidTag Le tag RFID du compte à recharger.
     * @param amount  Le montant à ajouter au solde.
     * @return true si la recharge est réussie, sinon false.
     */
    public boolean rechargeAccount(String rfidTag, double amount) {
        if (amount <= 0) {
            System.out.println("Le montant de la recharge doit être supérieur à 0 !");
            return false;
        }

        User user = userDAO.getUserByRfid(rfidTag);
        if (user != null) {
            double newBalance = user.getBalance() + amount;
            if (userDAO.updateBalance(rfidTag, newBalance)) {
                System.out.println("Recharge réussie ! Solde actuel : €" + newBalance);
                userDAO.addTransaction(rfidTag, "RECHARGE", amount);
                return true;
            }
        }
        System.out.println("Échec de la recharge : utilisateur non trouvé.");
        return false;
    }
}
