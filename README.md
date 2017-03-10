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


## Interface en ligne de commande 

Le programme propose une interface en ligne de commande permettant de jouer.

Plusieurs options sont disponibles pour modifier le comportement du jeu.

- ```m```
  * Syntaxe : ```-m <mode>```
  * Description : le mode de jeu
  * Valeur possible pour ```<mode>``` :
     * ```HvM``` (default) : l'homme affronte la machine
	 * ```HvH``` : l'homme affronte l'homme
	 * ```MvM``` : la machine affronte la machine
- ```ai```
  * Syntaxe : ```-ai <nom-ia>```
  * Description : l'IA a utiliser
  * Valeur possible pour ```<nom-ia>``` : voir la section IA implémentées
  * Note : cette option peut être utilisée deux fois dans le cas d'un mode MvM pour spécifier les deux IA a utiliser
- ```-ai-opts```
  * Syntaxe : ```--ai-opts <options>```
  * Description : les options de construction pour l'IA
  * Valeur possible pour ```<options>``` : dépend de chaque IA
  * Note : cette option peut être utilisée deux fois dans le cas d'un mode MvM pour spécifier les deux IA a utiliser

  
### Exemple 

Démarrer le jeu en mode 1 contre 1.

```bash
java -jar Omegathello.jar -m HvH
```

Démarrer le jeu avec une ia utilisant l'algorithme minmax à une profondeur de 4.

```bash
java -jar Omegathello.jar -ai minmax --ai-opts depth=4
```


Démarrer le jeu en mode machine vs. machine avec un MinMax contre un MonteCarlo.

```bash
java -jar Omegathello.jar -m MvM -ai minmax --ai-opts depth=5 -ai montecarlo --ai-opts num=100,time=500
```



## Création et ajout d'une IA

La création d'une IA se fait en dérivant de la classe AbstractAI du package ai. 
Il suffit de redéfinir les méthodes suivantes de la classe abstraite :

- notifyPass() : utilisée par l'interface pour indiquer à l'IA qu'elle va passer son tour ;
- notifyRewind() : utilisée par l'UI pour indiquer à l'IA que le jeu effectue un retour en arrière ;
- notifyMove() : utilisée par l'UI pour indiquer qu'un coup vient d'être joué (utiliser aussi bien pour l'IA elle-même que pour son adversaire) ;
- notifyLoad() : utilisée par l'UI pour indiquer à l'IA qu'une partie vient d'être chargée ;
- selectMoveWithTimeout() : demande à l'IA de sélectionner un coup parmi ceux jouable dans une limite de temps donnée.

Pour informer l'interface de l'existance de l'IA, il suffit : 

- de lui donner un nom (sans espace) permettant de l'identifier de manière unique en définnisant un attribut ```static``` et ```public``` de type ```String``` ayant pour nom ```NAME``` ;

```java
public static String NAME = "myai";
```

- d'ajouter dans la fonction ```availableAIs()``` du fichier Main.java la ligne correspondant à votre IA.

```java
ais.add(MyClassAI.class);
```


## IA implémentées

Plusieurs intelligences artificielles sont fournies par défaut : 

- ```minmax``` : une IA utilisant l'algorithme minmax avec un élagage alpha-beta ;
- ```montecarlo``` : une IA utilisant la recherche de MonteCarlo pour les arbres.


### IA MinMax

Cette IA utilise l'algorithme minmax pour décider quel coup jouer.
L'IA utilise une fonction d'évaluation pour évaluer la situation en milieux de partie. 
Elle explore l'arbre à une profondeur fixe, donnée en paramètre au constructeur avec l'option ```depth```, 
modulo un éventuel élagage.

#### Options de construction

- ```depth```
  * Syntaxe : ```depth=3```
  * Description : contrôle la profondeur d'exploration de l'arbre
- ```func```
  * Syntaxe : ```func=ssymef```
  * Description : indique la fonction d'évaluation utilisée pour évaluer le plateau
  * Fonctions disponibles :
     * ```ssymef``` (default) - une fonction d'évaluation symétrique très simple
	 
Note : les options sont séparés par des virgules.
	 
### IA MonteCarlo

Cette IA utilise la recherche d'arbre de MonteCarlo pour décider quel coup choisir.
L'IA explore "au hasard" les branches de l'arbre en jouant les parties jusqu'au bout, elle 
est alors en mesure de savoir quelle branche présente à priori la plus forte probabilité de victoire.
Le nombre de branches explorées à chaque étape peut être fixée grâce à l'option ```num```. Alternativement, 
un temps maximum d'exploration peut être définie avec l'option ```time```.


#### Options de construction

- ```num```
  * Syntaxe : ```num=64```
  * Description : Le nombre de branche à explorer à chaque étape (défaut:64).
  * Note : la valeur -1 peut être utilisée pour indiquer que seule la limite de temps est à prendre en compte
- ```time```
  * Syntaxe : ```time=750```
  * Description : indique le temps maximal (en millisecondes) autorisé pour l'exploration des branches (défaut:-1)
  * Note : la valeur -1 peut être utilisée pour indiquer que seule la limite en nombre de branche est à prendre en compte.
  
Note : les options ```num``` et ```time``` peuvent être combinés ; les options sont séparés par des virgules.

