<?php

require_once('php/setupdb.php');


$paradas=$_GET['paradas'];

$p=json_decode($paradas);


foreach($p as $pa){
	echo $pa->lat."<br/>";
}

?>