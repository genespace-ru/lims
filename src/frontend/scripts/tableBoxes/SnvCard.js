import React from 'react';
import { registerTableBox, Navs } from 'be5-react';

export default function SnvCard({ value }) 
{
    const groups = value.data.attributes.rows;

    let notempty = '';
    if( groups[0].notempty )
        notempty = '/_notempty_=true';
    
    let steps = [];
    for (const group of groups) 
    {
        let view = group.viewname.value;
        if( ! view )
            view = "SNV tab";
         
        let step = { title: group.title_ru.value,
                     url: "#!table/snv_/" + view + 
                     "/_tcloneid_=" + group.sample.value +
                     "/_snvid_="    + group.snv.value +
                     "/_groupid_="  + group.id.value + 
                     notempty }; 

        steps.push(step);
    }    

    let step = { title: "Все аттрибуты",
                 url: "#!table/snv_/SNV tab" + 
                 "/_tcloneid_=" + groups[0].sample.value +
                 "/_snvid_="    + groups[0].snv.value + 
                 notempty}; 
    steps.push(step);
    
  return (
    <div className="SnvCard">
      <Navs steps={steps} tabs startAtStep={0} />
    </div>
  );
};

registerTableBox('SnvCard', SnvCard);
