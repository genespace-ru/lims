import React from 'react';
import {useEffect, useState} from 'react';
import {registerTableBox} from 'be5-react';

export default function SnvDetails(props)
{
    const [attrs, setAttrs] = useState(null);
    
    useEffect(() => {
        setAttrs("ssss");
    }, []);    

    const json = props.value.data.attributes.rows[0].Attributes.value;
    const snv  = JSON.parse(json)
//    const s = frontendParams;
    return (
        <div>
           {attrs}
           <br/>
           {json}
        </div>
    )
}

registerTableBox('SnvDetails', SnvDetails);
