package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_TYPE = "^[MTC]{1}.*";
    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<Employe>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName) {
        Stream<String> stream;
        try {
            stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));

            //TODO

            Integer i = 0;
            for(String ligne : stream.collect(Collectors.toList())) {
                i++;
                try{
                    processLine(ligne);
                }catch (BatchException e){
                    System.out.println("Ligne " + i + " : " + e.getMessage() + " => " + ligne);
                }
            }
            stream.close();
        } catch (IOException e) {
            System.out.println("Erreur URI");
        }



        return employes;
    }
    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        String[] ligneEmploye = ligne.split(",");

        //TODO
        if(!ligne.matches(REGEX_TYPE)) {
            throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
        }
        if(!ligne.substring(0,6).matches(REGEX_MATRICULE)){
            throw new BatchException("la chaîne " + ligneEmploye[0] + " ne respecte pas l'expression régulière ^[MTC][0-9]{5}$");
        }

        if(ligneEmploye.length != NB_CHAMPS_MANAGER && ligneEmploye[0].matches("^[M].*")){
            throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_MANAGER + " éléments mais " + ligneEmploye.length );
        }

        if(ligneEmploye.length != NB_CHAMPS_COMMERCIAL && ligneEmploye[0].matches("^[C]{1}.*")){
            throw new BatchException("La ligne commercial ne contient pas " + NB_CHAMPS_COMMERCIAL + " éléments mais " + ligneEmploye.length );
        }

        if(ligneEmploye.length != NB_CHAMPS_TECHNICIEN && ligneEmploye[0].matches("^[T]{1}.*")){
            throw new BatchException("La ligne technicien ne contient pas " + NB_CHAMPS_TECHNICIEN + " éléments mais " + ligneEmploye.length );
        }

        try {
            DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(ligneEmploye[3]);
        } catch (Exception e) {
            throw new BatchException(ligneEmploye[3] + " ne respecte pas le format de date JJ/MM/AAAA" );
        }

        try{
            Double.parseDouble(ligneEmploye[4]);
        } catch (Exception e){
            throw new BatchException(ligneEmploye[4] + " n'est pas un nombre valide pour un salaire");
        }

        if(ligneEmploye[0].matches("^[C]{1}.*")){
            try {
                Double.parseDouble(ligneEmploye[5]);
            }catch(Exception e){
                throw new BatchException("Le chiffre d'affaire du commercial est incorrecte : " + ligneEmploye[5]);
            }
        }

        if(ligneEmploye[0].matches("^[C]{1}.*")){
            try{
                Double.parseDouble(ligneEmploye[6]);
            }catch(Exception e){
                throw new BatchException("La performance du commercial est incorrecte : " + ligneEmploye[6]);
            }
        }

        if(ligneEmploye[0].matches("^[T]{1}.*") && !ligneEmploye[5].matches("^[1-5]")){
            throw new BatchException("Le grade du technicien doit être compris entre 1 & 5 : " + ligneEmploye[5]);
        }

        if(ligneEmploye[0].matches("^[T]{1}.*") && !ligneEmploye[6].matches(REGEX_MATRICULE_MANAGER)){
                throw new BatchException("La chaîne " + ligneEmploye[6] + " ne respecte pas la regex " + REGEX_MATRICULE_MANAGER);
        }
/*
        if(ligneEmploye[0].matches("^[T]{1}.*")){
            Employe managerDuTechnicien = employeRepository.findByMatricule(ligneEmploye[6]);
            if() {
                throw new BatchException("Le manager de matricule " + ligneEmploye[6] + " n'a pas été trouvé dans " +
                        "le fichier ou la base de données ");
            }
        }

 */
        boolean managerExiste=false;
        if (ligneEmploye[0].charAt(0)=='T') {
            if(!ligneEmploye[6].matches(REGEX_MATRICULE_MANAGER))
                throw new BatchException("la chaîne "+ligneEmploye[6]+" ne respecte pas l'expression régulière "+REGEX_MATRICULE_MANAGER);

            Stream<String> stream;
            try {
                stream = Files.lines(Paths.get(new ClassPathResource("employes.csv").getURI()));

            for(String ligneln : stream.collect(Collectors.toList())) {
                String[] partsln = ligneln.split(",");
                if(ligneEmploye[6].equals(partsln[0])) {
                    managerExiste=true;
                    return;
                }
            }

            stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //

            Manager manager =managerRepository.findByMatricule(ligneEmploye[6]);
            if (manager ==null && managerExiste == false)
                throw new BatchException("Le manager de matricule "+ligneEmploye[6]+" n'a pas été trouvé dans le fichier ou en base de données ");
        }

    }

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO
    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO

    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        //TODO
    }

}
