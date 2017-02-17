# othelloecn
Github option info 2017 projet de groupe Othello


## Compilation avec ANT

Le projet peut facilement être compilé avec ANT en exécutant la commande ```ant``` à la racine du projet.

Les tâches effectués par ANT sont les suivantes :
 
- Compilation du projet 
- Compilation et exécution des tests unitaires avec JUnit
- Compilation de la Javadoc
- Calcul de la couverture de code avec Jacoco
- Evaluation de la qualité du code avec SonarQube

La bonne exécution du dernier point nécessite d'avoir SonarQube installé et lancé.



## Support de Netbeans

Il est possible d'ouvrir et de compiler/exécuter le projet avec Netbeans en effectuant les étapes suivantes :

1. Effectuer un ```git clone``` du projet
2. Dans Netbeans, créer un nouveau projet Java à partir de fichiers existants :
    * en sélectionnant pour dossier du projet le dossier créé par ```git clone```,
	* en choisissant comme nom de fichier de build un nom différent de ```build.xml``` (par ex. ```nbbuild.xml```),
    * puis ajoutant le dossier ```src``` (resp. ```test```) comme dossier de package des sources (resp. des tests)
3. Si Netbeans vous indique qu'il y a des problèmes non résolus, suivez simplement la procédure permettant de les résoudre

Vous devriez ensuite être capable de travailler sur le projet depuis Netbeans.
